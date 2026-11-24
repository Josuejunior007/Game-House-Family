package io.github.josuejunior007.gamehousefamily.repository;

import io.github.josuejunior007.gamehousefamily.model.Family;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FamilyRepository extends JpaRepository<Family, Integer> {

    /** O app é de uma família só: a primeira (e única) linha é a família. */
    Optional<Family> findFirstByOrderByIdAsc();
}
