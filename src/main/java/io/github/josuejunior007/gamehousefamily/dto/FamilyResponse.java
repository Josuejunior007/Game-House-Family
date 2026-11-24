package io.github.josuejunior007.gamehousefamily.dto;

import io.github.josuejunior007.gamehousefamily.model.Family;
import io.github.josuejunior007.gamehousefamily.model.FamilyMember;

import java.time.Instant;
import java.util.List;

public record FamilyResponse(Integer id, String name, Integer ownerUserId, String ownerName,
                             Instant createdAt, List<FamilyMemberResponse> members) {

    public static FamilyResponse de(Family familia, List<FamilyMember> membros) {
        List<FamilyMemberResponse> membrosOrdenados = membros.stream()
                .map(FamilyMemberResponse::de)
                .toList();
        return new FamilyResponse(familia.getId(), familia.getName(), familia.getOwner().getId(),
                familia.getOwner().getDisplayName(), familia.getCreatedAt(), membrosOrdenados);
    }
}
