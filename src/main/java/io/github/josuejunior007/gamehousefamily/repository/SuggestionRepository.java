package io.github.josuejunior007.gamehousefamily.repository;

import io.github.josuejunior007.gamehousefamily.model.Suggestion;
import io.github.josuejunior007.gamehousefamily.model.SuggestionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface SuggestionRepository extends JpaRepository<Suggestion, Integer> {

    boolean existsByGameIdAndStatus(int gameId, SuggestionStatus status);

    List<Suggestion> findAllByOrderByCreatedAtDesc();

    List<Suggestion> findByStatusOrderByCreatedAtDesc(SuggestionStatus status);

    List<Suggestion> findByStatusAndVotingDeadlineBefore(SuggestionStatus status, Instant limite);
}
