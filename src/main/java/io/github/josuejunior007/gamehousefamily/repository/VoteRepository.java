package io.github.josuejunior007.gamehousefamily.repository;

import io.github.josuejunior007.gamehousefamily.model.Vote;
import io.github.josuejunior007.gamehousefamily.model.VoteChoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VoteRepository extends JpaRepository<Vote, Integer> {

    Optional<Vote> findByUserIdAndSuggestionId(int userId, int suggestionId);

    long countBySuggestionIdAndChoice(int suggestionId, VoteChoice choice);
}
