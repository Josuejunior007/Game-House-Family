package io.github.josuejunior007.gamehousefamily.service;

import io.github.josuejunior007.gamehousefamily.dto.GameCreateRequest;
import io.github.josuejunior007.gamehousefamily.dto.GamePriceResponse;
import io.github.josuejunior007.gamehousefamily.dto.GameSearchItem;
import io.github.josuejunior007.gamehousefamily.dto.GameUpdateRequest;
import io.github.josuejunior007.gamehousefamily.dto.SteamGameData;
import io.github.josuejunior007.gamehousefamily.dto.SteamSearchResult;
import io.github.josuejunior007.gamehousefamily.exception.GameIdNaoEncontradoNoBancoException;
import io.github.josuejunior007.gamehousefamily.exception.GameJaCadastradoException;
import io.github.josuejunior007.gamehousefamily.exception.GameNaoEncontradoNaSteamException;
import io.github.josuejunior007.gamehousefamily.exception.SteamIndisponivelException;
import io.github.josuejunior007.gamehousefamily.model.Game;
import io.github.josuejunior007.gamehousefamily.repository.GameRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class GameService {

    /** Mesmo teto da busca da Steam: não faz sentido devolver mais que isso para escolher um jogo. */
    private static final int LIMITE_BUSCA = 10;

    private final GameRepository repository;
    private final SteamService steamService;

    public GameService(GameRepository repository, SteamService steamService) {
        this.repository = repository;
        this.steamService = steamService;
    }

    /**
     * Sem @Transactional de propósito: a chamada à Steam pode levar segundos e não deve
     * segurar uma conexão do pool. O existsBy + save já são atômicos por si.
     */
    public Game cadastrar(GameCreateRequest request) {

        if (repository.existsBySteamAppId(request.steamAppId())) {
            throw new GameJaCadastradoException("Jogo já cadastrado");
        }

        SteamGameData dadosSteam = steamService.buscarJogo(request.steamAppId())
                .orElseThrow(() -> new GameNaoEncontradoNaSteamException("Jogo não foi encontrado"));

        return repository.save(new Game(request.steamAppId(), dadosSteam.name(), dadosSteam.headerImage()));
    }

    /**
     * Busca para escolher um jogo (catálogo/sugestão): olha primeiro o banco, que é instantâneo.
     * Se faltar resultado, vai à Steam e guarda o que encontrou, para a próxima busca ser local.
     * Se a Steam falhar mas o banco já tiver algo, devolve o que o banco tem em vez de erro.
     */
    public List<GameSearchItem> buscar(String termo) {
        String busca = termo == null ? "" : termo.trim();
        if (busca.length() < 2) {
            return List.of();
        }

        List<Game> locais = repository.findTop10ByNameContainingIgnoreCaseOrderByNameAsc(busca);
        List<GameSearchItem> itens = new ArrayList<>(locais.stream().map(GameSearchItem::doBanco).toList());
        if (itens.size() >= LIMITE_BUSCA) {
            return itens;
        }

        List<SteamSearchResult> achados;
        try {
            achados = steamService.buscarJogos(busca, LIMITE_BUSCA);
        } catch (SteamIndisponivelException e) {
            if (!itens.isEmpty()) {
                return itens;
            }
            throw e;
        }

        Set<String> vistos = new HashSet<>(locais.stream().map(Game::getSteamAppId).toList());
        for (SteamSearchResult achado : achados) {
            if (itens.size() >= LIMITE_BUSCA) {
                break;
            }
            String appId = String.valueOf(achado.steamAppId());
            if (!vistos.add(appId)) {
                continue;
            }
            itens.add(GameSearchItem.daSteam(salvarOuBuscar(appId, achado), achado));
        }

        return itens;
    }

    /**
     * A busca da Steam já devolve nome e miniatura, então o jogo é salvo sem outra chamada.
     * Duas buscas simultâneas do mesmo jogo caem na unique de steam_app_id: nesse caso
     * aproveitamos a linha que a outra requisição gravou.
     */
    private Game salvarOuBuscar(String appId, SteamSearchResult achado) {
        return repository.findBySteamAppId(appId).orElseGet(() -> {
            try {
                return repository.save(new Game(appId, achado.name(), capaMaior(achado.imageUrl(), appId)));
            } catch (DataIntegrityViolationException corrida) {
                return repository.findBySteamAppId(appId).orElseThrow(() -> corrida);
            }
        });
    }

    /**
     * A busca devolve a miniatura (capsule_231x87), às vezes dentro de uma pasta com hash.
     * A capa 460x215 dos cartões fica no mesmo CDN, em /steam/apps/{appId}/header.jpg.
     * Se a URL não tiver o formato esperado, fica a miniatura mesmo — melhor que nada.
     */
    private String capaMaior(String url, String appId) {
        if (url == null || url.isBlank()) {
            return null;
        }
        int corte = url.indexOf("/steam/apps/");
        if (corte < 0) {
            return url;
        }
        return url.substring(0, corte) + "/steam/apps/" + appId + "/header.jpg";
    }

    @Transactional(readOnly = true)
    public List<Game> listar() {

        return repository.findAllByOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public Game buscarPorID(Integer id) {

        return repository.findById(id)
                .orElseThrow(() -> new GameIdNaoEncontradoNoBancoException("Jogo não encontrado: " + id));
    }

    @Transactional
    public Game atualizar(Integer id, GameUpdateRequest request) {

        Game game = repository.findById(id)
                .orElseThrow(() -> new GameIdNaoEncontradoNoBancoException("Jogo não encontrado: " + id));

        game.atualizarNome(request.name()); // entidade gerenciada: o JPA grava no fim da transação
        return game;
    }

    /** O preço muda toda hora, então é consultado na Steam sob demanda em vez de salvo no banco. */
    @Transactional(readOnly = true)
    public GamePriceResponse buscarPreco(Integer id) {
        Game game = repository.findById(id)
                .orElseThrow(() -> new GameIdNaoEncontradoNoBancoException("Jogo não encontrado: " + id));

        SteamGameData dados = steamService.buscarJogo(game.getSteamAppId())
                .orElseThrow(() -> new GameNaoEncontradoNaSteamException("Jogo não foi encontrado na Steam"));

        return GamePriceResponse.de(dados);
    }
}
