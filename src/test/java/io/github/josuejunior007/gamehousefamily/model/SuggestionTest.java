package io.github.josuejunior007.gamehousefamily.model;

import io.github.josuejunior007.gamehousefamily.exception.SugestaoStatusInvalidoException;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SuggestionTest {

    private Suggestion nova(Instant prazo) {
        User user = new User("76561198000000001", "Ana", null);
        Game game = new Game("620", "Portal 2");
        return new Suggestion(user, game, 1, prazo);
    }

    @Test
    void nasceEmVotacao() {
        Suggestion s = nova(Instant.now().plusSeconds(3600));
        assertThat(s.getStatus()).isEqualTo(SuggestionStatus.EM_VOTACAO);
        assertThat(s.votacaoAberta(Instant.now())).isTrue();
    }

    @Test
    void votacaoFechaQuandoPrazoVence() {
        Suggestion s = nova(Instant.now().minusSeconds(1));
        assertThat(s.votacaoAberta(Instant.now())).isFalse();
        assertThat(s.prazoVencido(Instant.now())).isTrue();
    }

    @Test
    void fluxoCompletoAprovadaDepoisComprada() {
        Suggestion s = nova(Instant.now().plusSeconds(3600));
        s.aprovar();
        assertThat(s.getStatus()).isEqualTo(SuggestionStatus.APROVADA);
        s.marcarComoComprada();
        assertThat(s.getStatus()).isEqualTo(SuggestionStatus.COMPRADA);
    }

    @Test
    void naoPodeComprarSemAprovar() {
        Suggestion s = nova(Instant.now().plusSeconds(3600));
        assertThrows(SugestaoStatusInvalidoException.class, s::marcarComoComprada);
    }

    @Test
    void naoPodeAprovarDuasVezes() {
        Suggestion s = nova(Instant.now().plusSeconds(3600));
        s.aprovar();
        assertThrows(SugestaoStatusInvalidoException.class, s::aprovar);
        assertThrows(SugestaoStatusInvalidoException.class, s::rejeitar);
    }
}
