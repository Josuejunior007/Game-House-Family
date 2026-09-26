package io.github.josuejunior007.gamehousefamily.controller;

import io.github.josuejunior007.gamehousefamily.model.Game;
import io.github.josuejunior007.gamehousefamily.service.GameService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GameController {

    private final GameService buscador;

    public GameController(GameService buscador) {
        this.buscador = buscador;
    }

    @GetMapping("/game")
    public Game buscar() {
        return buscador.getGame();
    }
}
