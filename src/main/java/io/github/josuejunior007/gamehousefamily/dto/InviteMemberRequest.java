package io.github.josuejunior007.gamehousefamily.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record InviteMemberRequest(
        @NotBlank(message = "Informe o SteamID64")
        @Pattern(regexp = "\\d{17}", message = "O SteamID64 tem 17 números (começa com 7656119...)")
        String steamId) {
}
