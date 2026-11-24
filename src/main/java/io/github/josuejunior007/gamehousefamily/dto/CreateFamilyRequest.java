package io.github.josuejunior007.gamehousefamily.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateFamilyRequest(
        @NotBlank(message = "Dê um nome para a família")
        @Size(min = 2, max = 60, message = "O nome deve ter entre 2 e 60 caracteres")
        String name) {
}
