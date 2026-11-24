package io.github.josuejunior007.gamehousefamily.model;

/**
 * Ciclo de vida de um convite. O convite nasce PENDENTE e termina ACEITO (virou membro),
 * RECUSADO (a pessoa não quis) ou CANCELADO (o dono desistiu).
 */
public enum InviteStatus {
    PENDENTE,
    ACEITO,
    RECUSADO,
    CANCELADO
}
