package io.github.josuejunior007.gamehousefamily.controller;

import io.github.josuejunior007.gamehousefamily.config.AuthInterceptor;
import io.github.josuejunior007.gamehousefamily.dto.SuggestionCreateRequest;
import io.github.josuejunior007.gamehousefamily.dto.SuggestionResponse;
import io.github.josuejunior007.gamehousefamily.model.SuggestionStatus;
import io.github.josuejunior007.gamehousefamily.service.SuggestionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class SuggestionController {

    private final SuggestionService suggestionService;

    public SuggestionController(SuggestionService suggestionService) {
        this.suggestionService = suggestionService;
    }

    @PostMapping("/suggestions")
    public ResponseEntity<SuggestionResponse> criar(@SessionAttribute(AuthInterceptor.SESSION_USER_ID) Integer userId,
                                                    @RequestBody @Valid SuggestionCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(suggestionService.criar(userId, request));
    }

    @GetMapping("/suggestions")
    public List<SuggestionResponse> listar(@SessionAttribute(AuthInterceptor.SESSION_USER_ID) Integer userId,
                                           @RequestParam(required = false) SuggestionStatus status) {
        return suggestionService.listar(status, userId);
    }

    @GetMapping("/suggestions/{id}")
    public SuggestionResponse buscar(@SessionAttribute(AuthInterceptor.SESSION_USER_ID) Integer userId,
                                     @PathVariable Integer id) {
        return suggestionService.buscarPorId(id, userId);
    }

    @PostMapping("/suggestions/{id}/close")
    public SuggestionResponse encerrar(@SessionAttribute(AuthInterceptor.SESSION_USER_ID) Integer userId,
                                       @PathVariable Integer id) {
        return suggestionService.encerrar(id, userId);
    }

    @PostMapping("/suggestions/{id}/purchase")
    public SuggestionResponse comprar(@SessionAttribute(AuthInterceptor.SESSION_USER_ID) Integer userId,
                                      @PathVariable Integer id) {
        return suggestionService.marcarComoComprada(id, userId);
    }

    @DeleteMapping("/suggestions/{id}")
    public ResponseEntity<Void> excluir(@SessionAttribute(AuthInterceptor.SESSION_USER_ID) Integer userId,
                                        @PathVariable Integer id) {
        suggestionService.excluir(id, userId);
        return ResponseEntity.noContent().build();
    }
}
