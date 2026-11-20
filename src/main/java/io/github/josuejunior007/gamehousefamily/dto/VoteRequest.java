package io.github.josuejunior007.gamehousefamily.dto;

import io.github.josuejunior007.gamehousefamily.model.VoteChoice;
import jakarta.validation.constraints.NotNull;

public record VoteRequest(@NotNull(message = "Informe APOIO ou NAO_APOIO") VoteChoice choice) {
}
