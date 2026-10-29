package io.github.josuejunior007.gamehousefamily.repository;

import io.github.josuejunior007.gamehousefamily.model.Suggestion;
import io.github.josuejunior007.gamehousefamily.model.SuggestionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SuggestionRepository extends JpaRepository<Suggestion, Integer> {

    boolean existsByGameIdAndStatus(int gameId, SuggestionStatus status);
}
