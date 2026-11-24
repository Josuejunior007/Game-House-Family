package io.github.josuejunior007.gamehousefamily.repository;

import io.github.josuejunior007.gamehousefamily.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GameRepository extends JpaRepository<Game, Integer> {

    boolean existsBySteamAppId(String steamAppId);

    Optional<Game> findBySteamAppId(String steamAppId);

    /** Catálogo em ordem alfabética: sem ORDER BY o Postgres devolve na ordem física, que muda sem avisar. */
    List<Game> findAllByOrderByNameAsc();

    /** Busca local (mais rápida que a Steam) usada antes de ir para a loja. */
    List<Game> findTop10ByNameContainingIgnoreCaseOrderByNameAsc(String nome);
}
