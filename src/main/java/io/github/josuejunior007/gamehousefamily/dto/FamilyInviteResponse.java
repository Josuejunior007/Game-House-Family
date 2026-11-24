package io.github.josuejunior007.gamehousefamily.dto;

import io.github.josuejunior007.gamehousefamily.model.FamilyInvite;
import io.github.josuejunior007.gamehousefamily.model.InviteStatus;

import java.time.Instant;

public record FamilyInviteResponse(Integer id, Integer familyId, String familyName, String steamId,
                                   String invitedName, String invitedAvatarUrl, String invitedByName,
                                   InviteStatus status, Instant createdAt) {

    public static FamilyInviteResponse de(FamilyInvite convite) {
        return new FamilyInviteResponse(convite.getId(), convite.getFamily().getId(),
                convite.getFamily().getName(), convite.getSteamId(), convite.getInvitedName(),
                convite.getInvitedAvatarUrl(), convite.getInvitedBy().getDisplayName(),
                convite.getStatus(), convite.getCreatedAt());
    }
}
