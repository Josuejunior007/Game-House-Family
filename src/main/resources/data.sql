-- RN-09: o banco também garante "no máximo 1 sugestão EM_VOTACAO por jogo" (índice único parcial do Postgres).
-- O histórico continua livre: o índice só enxerga as linhas com status EM_VOTACAO.
CREATE UNIQUE INDEX IF NOT EXISTS ux_suggestion_one_active_per_game
    ON suggestion (game_id)
    WHERE status = 'EM_VOTACAO';
