package io.github.josuejunior007.gamehousefamily.dto;

import io.github.josuejunior007.gamehousefamily.model.FamilyRole;

/** Resumo que vai dentro de UserResponse, para o front saber em que família a pessoa está sem outra chamada. */
public record FamilySummary(Integer id, String name, FamilyRole role) {
}
