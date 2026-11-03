package io.github.josuejunior007.gamehousefamily.dto;

import io.github.josuejunior007.gamehousefamily.model.Suggestion;
import io.github.josuejunior007.gamehousefamily.model.SuggestionStatus;


import java.time.Instant;

public record SuggestionResponse(Integer id, String userDisplayName, Integer userId, String gameName, Integer gameId,
                                 Integer copies, SuggestionStatus status, Instant votingDeadline, Instant createdAt) {

    public static SuggestionResponse de(Suggestion suggestion) {
        return new SuggestionResponse(suggestion.getId(), suggestion.getUser().getDisplayName(),
                suggestion.getUser().getId(), suggestion.getGame().getName(), suggestion.getGame().getId(),
                suggestion.getCopies(), suggestion.getStatus(), suggestion.getVotingDeadline(), suggestion
                .getCreatedAt());
    }
}
