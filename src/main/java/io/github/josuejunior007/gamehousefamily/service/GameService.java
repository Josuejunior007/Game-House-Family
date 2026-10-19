package io.github.josuejunior007.gamehousefamily.service;

import io.github.josuejunior007.gamehousefamily.dto.GameCreateRequest;
import io.github.josuejunior007.gamehousefamily.dto.GameUpdateRequest;
import io.github.josuejunior007.gamehousefamily.dto.SteamGameData;
import io.github.josuejunior007.gamehousefamily.model.Game;
import io.github.josuejunior007.gamehousefamily.repository.GameRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

import java.util.List;

@Service
public class GameService {

    private final GameRepository repository;
    private final SteamService steamService;

    public GameService(GameRepository repository, SteamService steamService) {
        this.repository = repository;
        this.steamService = steamService;
    }


    public Game cadastrar(GameCreateRequest request) {

        if (repository.existsBySteamAppId(request.steamAppId())) {
            throw new RuntimeException("Jogo já cadastrado"); // TODO: virar 409
        }

        SteamGameData dadosSteam = steamService.buscarJogo(request.steamAppId())
                .orElseThrow(() -> new RuntimeException("Jogo não encontrado na Steam")); // TODO: virar 404

        Game game = new Game(request.steamAppId(), dadosSteam.name());

        return repository.save(game);
    }

    public List<Game> listar() {

        return repository.findAll();
    }

    public Optional<Game> buscarPorID( Integer id) {

        Optional<Game> game = repository.findById(id);

        return game;
    }
    public Optional<Game> atualizar(Integer id, GameUpdateRequest request) {
        Optional<Game> game = repository.findById(id);

        if (game.isPresent()) {

            Game gameEncontrado = game.get();

            gameEncontrado.atualizarNome(request.name());

            repository.save(gameEncontrado);
            return Optional.of(gameEncontrado);
        }
        return Optional.empty();
    }


}
