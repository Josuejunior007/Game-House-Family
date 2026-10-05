package io.github.josuejunior007.gamehousefamily.controller;

import io.github.josuejunior007.gamehousefamily.model.Game;
import io.github.josuejunior007.gamehousefamily.service.GameService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

import java.util.List;

@RestController
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }


    @PostMapping("/games")
    public Game cadastrar(@RequestBody Game game) {
        return gameService.cadastrar(game);
    }
    @GetMapping("/games")
    public List<Game> listar() {
        return  gameService.listar();
    }
    @GetMapping("/games/{id}")
    public ResponseEntity<Game> buscar(@PathVariable Integer id) {

        Optional<Game> game = gameService.buscarPorID(id);

        if (game.isPresent()) {
            return ResponseEntity.ok(game.get());
        } else {
            return ResponseEntity.notFound().build();
        }

    }

}
