package io.github.josuejunior007.gamehousefamily.service;

import io.github.josuejunior007.gamehousefamily.model.Game;
import io.github.josuejunior007.gamehousefamily.repository.GameRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GameService {

    private final GameRepository repository;

    public GameService(GameRepository repository) {
        this.repository = repository;
    }

    public Game cadastrar(Game game) {
        return repository.save(game);
    }

    public List<Game> listar() {

        return repository.findAll();
    }
}
