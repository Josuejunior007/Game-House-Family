package io.github.josuejunior007.gamehousefamily.dto;

import jakarta.validation.constraints.NotBlank;

public record GameUpdateRequest(@NotBlank String name) {

}
