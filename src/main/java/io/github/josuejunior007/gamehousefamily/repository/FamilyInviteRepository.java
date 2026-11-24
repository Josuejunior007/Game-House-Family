package io.github.josuejunior007.gamehousefamily.repository;

import io.github.josuejunior007.gamehousefamily.model.FamilyInvite;
import io.github.josuejunior007.gamehousefamily.model.InviteStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FamilyInviteRepository extends JpaRepository<FamilyInvite, Integer> {

    @EntityGraph(attributePaths = {"family", "invitedBy"})
    List<FamilyInvite> findBySteamIdAndStatusOrderByCreatedAtDesc(String steamId, InviteStatus status);

    @EntityGraph(attributePaths = {"family", "invitedBy"})
    List<FamilyInvite> findByFamilyIdAndStatusOrderByCreatedAtDesc(int familyId, InviteStatus status);

    boolean existsByFamilyIdAndSteamIdAndStatus(int familyId, String steamId, InviteStatus status);

    void deleteByFamilyId(int familyId);
}
