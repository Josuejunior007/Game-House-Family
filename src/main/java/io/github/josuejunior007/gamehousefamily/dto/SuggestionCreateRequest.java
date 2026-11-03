package io.github.josuejunior007.gamehousefamily.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SuggestionCreateRequest(@NotNull Integer gameId, @NotNull Integer userId,
                                      @NotNull @Min(value = 1, message = "O número de cópias deve ser maior que 0") Integer copies,
                                      @NotNull @Min(value = 1, message = "O prazo deve ser de no mínimo 1 dia")
                                      @Max(value = 30, message = "não pode ultrapassar 30 dias") Integer votingDays) {
}
