package io.github.josuejunior007.gamehousefamily.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DevLoginRequest(@NotBlank @Size(max = 64, message = "O steamId deve ter no máximo 64 caracteres")
                              String steamId,
                              @NotBlank @Size(max = 255, message = "O nome deve ter no máximo 255 caracteres")
                              String displayName) {
}
