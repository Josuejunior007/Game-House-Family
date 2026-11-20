package io.github.josuejunior007.gamehousefamily.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GameCreateRequest(@NotBlank @Size(max = 64, message = "O appId deve ter no máximo 64 caracteres")
                                String steamAppId) {
}
