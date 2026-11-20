package io.github.josuejunior007.gamehousefamily.service;

import io.github.josuejunior007.gamehousefamily.dto.GameCreateRequest;
import io.github.josuejunior007.gamehousefamily.dto.GamePriceResponse;
import io.github.josuejunior007.gamehousefamily.dto.GameUpdateRequest;
import io.github.josuejunior007.gamehousefamily.dto.SteamGameData;
import io.github.josuejunior007.gamehousefamily.exception.GameIdNaoEncontradoNoBancoException;
import io.github.josuejunior007.gamehousefamily.exception.GameJaCadastradoException;
import io.github.josuejunior007.gamehousefamily.exception.GameNaoEncontradoNaSteamException;
import io.github.josuejunior007.gamehousefamily.model.Game;
import io.github.josuejunior007.gamehousefamily.repository.GameRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GameService {

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
