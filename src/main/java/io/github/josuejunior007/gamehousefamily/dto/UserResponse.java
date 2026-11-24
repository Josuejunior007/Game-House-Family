package io.github.josuejunior007.gamehousefamily.dto;

import io.github.josuejunior007.gamehousefamily.model.FamilyRole;
import io.github.josuejunior007.gamehousefamily.model.User;

public record UserResponse(Integer id, String steamId, String displayName, String avatarUrl,
                           Integer familyId, String familyName, FamilyRole familyRole) {

    public static UserResponse de(User user, FamilySummary resumo) {
        return new UserResponse(user.getId(), user.getSteamId(), user.getDisplayName(), user.getAvatarUrl(),
                resumo == null ? null : resumo.id(),
                resumo == null ? null : resumo.name(),
                resumo == null ? null : resumo.role());
    }
}
