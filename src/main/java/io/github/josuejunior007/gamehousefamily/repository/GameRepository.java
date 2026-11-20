package io.github.josuejunior007.gamehousefamily.repository;

import io.github.josuejunior007.gamehousefamily.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GameRepository extends JpaRepository<Game, Integer> {

    boolean existsBySteamAppId(String steamAppId);

    /** Catálogo em ordem alfabética: sem ORDER BY o Postgres devolve na ordem física, que muda sem avisar. */
    List<Game> findAllByOrderByNameAsc();
}
