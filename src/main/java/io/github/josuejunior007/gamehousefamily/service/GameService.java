package io.github.josuejunior007.gamehousefamily.service;

import io.github.josuejunior007.gamehousefamily.model.Game;
import io.github.josuejunior007.gamehousefamily.repository.GameRepository;
import org.springframework.stereotype.Service;

@Service
public class GameService {

    private final GameRepository repository;

    public GameService(GameRepository repository) {
        this.repository = repository;
    }

    public Game getGame(){

        Game REMATCH = new Game("2138720", "REMATCH");

        return repository.save(REMATCH);
    }
}
