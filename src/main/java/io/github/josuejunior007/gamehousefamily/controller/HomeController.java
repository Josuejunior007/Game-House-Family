package io.github.josuejunior007.gamehousefamily.controller;

import io.github.josuejunior007.gamehousefamily.model.Game;
import io.github.josuejunior007.gamehousefamily.service.GameService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home(){

        return "Game House Family";
    }

    private final GameService buscador;

    public HomeController(GameService buscador){
        this.buscador = buscador;
    }

    @GetMapping("/game")
    public Game game(){

    return buscador.getGame();
    }



}
