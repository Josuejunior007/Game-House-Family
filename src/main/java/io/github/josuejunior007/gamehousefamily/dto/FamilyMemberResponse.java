package io.github.josuejunior007.gamehousefamily.dto;

import io.github.josuejunior007.gamehousefamily.model.FamilyMember;
import io.github.josuejunior007.gamehousefamily.model.FamilyRole;

import java.time.Instant;

public record FamilyMemberResponse(Integer userId, String steamId, String displayName, String avatarUrl,
                                   FamilyRole role, Instant joinedAt) {

    public static FamilyMemberResponse de(FamilyMember membro) {
        return new FamilyMemberResponse(membro.getUser().getId(), membro.getUser().getSteamId(),
                membro.getUser().getDisplayName(), membro.getUser().getAvatarUrl(),
                membro.getRole(), membro.getJoinedAt());
    }
}
