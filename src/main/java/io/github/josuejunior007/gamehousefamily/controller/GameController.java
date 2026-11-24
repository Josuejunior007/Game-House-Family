package io.github.josuejunior007.gamehousefamily.controller;

import io.github.josuejunior007.gamehousefamily.dto.GameCreateRequest;
import io.github.josuejunior007.gamehousefamily.dto.GamePriceResponse;
import io.github.josuejunior007.gamehousefamily.dto.GameResponse;
import io.github.josuejunior007.gamehousefamily.dto.GameSearchItem;
import io.github.josuejunior007.gamehousefamily.dto.GameUpdateRequest;
import io.github.josuejunior007.gamehousefamily.service.GameService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping("/games")
    public ResponseEntity<GameResponse> cadastrar(@RequestBody @Valid GameCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(GameResponse.de(gameService.cadastrar(request)));
    }

    @GetMapping("/games")
    public List<GameResponse> listar() {
        return gameService.listar()
                .stream()
                .map(GameResponse::de)
                .toList();
    }

    /**
     * Busca por nome com cache no banco: primeiro o catálogo local, depois a Steam (o que vier
     * de lá já é salvo). O retorno sempre tem gameId, então serve direto para criar sugestão.
     */
    @GetMapping("/games/search")
    public List<GameSearchItem> buscar(@RequestParam(name = "q", required = false) String termo) {
        return gameService.buscar(termo);
    }

    @GetMapping("/games/{id}")
    public GameResponse buscar(@PathVariable Integer id) {
        return GameResponse.de(gameService.buscarPorID(id));
    }

    @PatchMapping("/games/{id}")
    public GameResponse atualizar(@PathVariable Integer id, @RequestBody @Valid GameUpdateRequest request) {
        return GameResponse.de(gameService.atualizar(id, request));
    }

    @GetMapping("/games/{id}/price")
    public GamePriceResponse preco(@PathVariable Integer id) {
        return gameService.buscarPreco(id);
    }
}
