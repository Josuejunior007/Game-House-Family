package io.github.josuejunior007.gamehousefamily.controller;

import io.github.josuejunior007.gamehousefamily.dto.GameUpdateRequest;
import io.github.josuejunior007.gamehousefamily.model.Game;
import io.github.josuejunior007.gamehousefamily.service.GameService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;
import org.springframework.web.bind.annotation.PatchMapping;

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

        return ResponseEntity.of(game);

    }
    @PatchMapping("/games/{id}")
    public ResponseEntity<Game> atualizar(@PathVariable Integer id, @RequestBody GameUpdateRequest request) {

        Optional<Game> game =gameService.atualizar(id, request);

        if(game.isPresent()) {
            return ResponseEntity.ok(game.get());
        }
        return ResponseEntity.notFound().build();
    }

}
