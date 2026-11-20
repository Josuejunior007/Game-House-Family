package io.github.josuejunior007.gamehousefamily.controller;

import io.github.josuejunior007.gamehousefamily.config.AuthInterceptor;
import io.github.josuejunior007.gamehousefamily.dto.SuggestionResponse;
import io.github.josuejunior007.gamehousefamily.dto.VoteRequest;
import io.github.josuejunior007.gamehousefamily.service.SuggestionService;
import io.github.josuejunior007.gamehousefamily.service.VoteService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
public class VoteController {

    private final VoteService voteService;
    private final SuggestionService suggestionService;

    public VoteController(VoteService voteService, SuggestionService suggestionService) {
        this.voteService = voteService;
        this.suggestionService = suggestionService;
    }

    // PUT porque é idempotente: repetir a mesma chamada deixa o voto no mesmo estado (cria ou troca)
    @PutMapping("/suggestions/{suggestionId}/vote")
    public SuggestionResponse votar(@SessionAttribute(AuthInterceptor.SESSION_USER_ID) Integer userId,
                                    @PathVariable Integer suggestionId,
                                    @RequestBody @Valid VoteRequest request) {
        voteService.votar(userId, suggestionId, request.choice());
        return suggestionService.buscarPorId(suggestionId, userId); // placar atualizado + meu voto
    }
}
