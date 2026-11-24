package io.github.josuejunior007.gamehousefamily.repository;

import io.github.josuejunior007.gamehousefamily.model.FamilyMember;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FamilyMemberRepository extends JpaRepository<FamilyMember, Integer> {

    /** Traz family e user juntos: quem chama precisa dos dois e não pode contar com sessão aberta. */
    @EntityGraph(attributePaths = {"family", "user"})
    Optional<FamilyMember> findByUserId(int userId);

    boolean existsByUserId(int userId);

    @EntityGraph(attributePaths = "user")
    List<FamilyMember> findByFamilyId(int familyId);

    void deleteByFamilyId(int familyId);
}
