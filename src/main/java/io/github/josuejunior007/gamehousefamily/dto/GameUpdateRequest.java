package io.github.josuejunior007.gamehousefamily.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GameUpdateRequest(@NotBlank @Size(max = 255, message = "O nome deve ter no máximo 255 caracteres")
                                String name) {
}
