package io.github.josuejunior007.gamehousefamily.dto;

import io.github.josuejunior007.gamehousefamily.model.User;

public record UserResponse(Integer id, String steamId, String displayName, String avatarUrl) {

    public static UserResponse de(User user) {
        return new UserResponse(user.getId(), user.getSteamId(), user.getDisplayName(), user.getAvatarUrl());
    }
}
