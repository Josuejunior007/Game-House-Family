package io.github.josuejunior007.gamehousefamily.dto;

import io.github.josuejunior007.gamehousefamily.model.Suggestion;
import io.github.josuejunior007.gamehousefamily.model.SuggestionStatus;
import io.github.josuejunior007.gamehousefamily.model.VoteChoice;

import java.time.Instant;

public record SuggestionResponse(Integer id, String userDisplayName, Integer userId, String userAvatarUrl,
                                 String gameName, Integer gameId, String gameImageUrl, String gameStoreUrl,
                                 Integer copies, SuggestionStatus status, Instant votingDeadline, Instant createdAt,
                                 long supportVotes, long opposeVotes, VoteChoice myVote) {

    public static SuggestionResponse de(Suggestion suggestion, long supportVotes, long opposeVotes, VoteChoice myVote) {
        String appId = suggestion.getGame().getSteamAppId();
        return new SuggestionResponse(suggestion.getId(), suggestion.getUser().getDisplayName(),
                suggestion.getUser().getId(), suggestion.getUser().getAvatarUrl(),
                suggestion.getGame().getName(), suggestion.getGame().getId(),
                SteamLinks.imagemDoJogo(suggestion.getGame()), SteamLinks.loja(appId),
                suggestion.getCopies(), suggestion.getStatus(), suggestion.getVotingDeadline(),
                suggestion.getCreatedAt(), supportVotes, opposeVotes, myVote);
    }
}
