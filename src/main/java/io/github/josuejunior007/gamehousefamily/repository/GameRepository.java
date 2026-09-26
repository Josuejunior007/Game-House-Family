package io.github.josuejunior007.gamehousefamily.repository;


import io.github.josuejunior007.gamehousefamily.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepository extends JpaRepository<Game, Integer> {
}
