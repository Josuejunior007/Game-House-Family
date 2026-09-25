package io.github.josuejunior007.gamehousefamily.service;

import io.github.josuejunior007.gamehousefamily.model.Game;
import org.springframework.stereotype.Service;

@Service
public class GameService {

    public Game getGame(){

        Game REMATCH = new Game(1, "2138720", "REMATCH");

        return REMATCH;
    }
}
