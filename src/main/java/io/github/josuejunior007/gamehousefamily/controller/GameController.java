package io.github.josuejunior007.gamehousefamily.controller;

import io.github.josuejunior007.gamehousefamily.dto.GameCreateRequest;
import io.github.josuejunior007.gamehousefamily.dto.GameResponse;
import io.github.josuejunior007.gamehousefamily.dto.GameUpdateRequest;
import io.github.josuejunior007.gamehousefamily.service.GameService;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<GameResponse> cadastrar(@RequestBody GameCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(GameResponse.de(gameService.cadastrar(request)));
    }
    @GetMapping("/games")
    public List<GameResponse> listar() {
        List<GameResponse> games = gameService.listar()
                .stream()
                .map(game -> GameResponse.de(game))
                .toList();
        return games;
    }
    @GetMapping("/games/{id}")
    public ResponseEntity<GameResponse> buscar(@PathVariable Integer id) {

        Optional<GameResponse> resposta = gameService.buscarPorID(id)
                .map(game -> GameResponse.de(game));

        return ResponseEntity.of(resposta);
    }
    @PatchMapping("/games/{id}")
    public ResponseEntity<GameResponse> atualizar(@PathVariable Integer id, @RequestBody GameUpdateRequest request) {
        Optional<GameResponse> atualizar = gameService.atualizar(id, request)
                .map(game -> GameResponse.de(game));
        return ResponseEntity.of(atualizar);
    }
}
