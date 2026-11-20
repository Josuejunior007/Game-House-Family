package io.github.josuejunior007.gamehousefamily.model;

import io.github.josuejunior007.gamehousefamily.exception.SugestaoStatusInvalidoException;
import jakarta.persistence.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Entity
public class Suggestion {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",  nullable = false)
    private User user;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id",nullable = false)
    private Game game;
    private int copies;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SuggestionStatus status;
    @Column(nullable = false)
    private Instant votingDeadline;
    @Column(nullable = false)
    private Instant createdAt;

    public Suggestion(User user, Game game, int copies, Instant votingDeadline) {
        this.user = user;
        this.game = game;
        this.copies = copies;
        // O Postgres guarda no máximo microssegundos: truncar aqui evita o mesmo instante
        // sair com precisão diferente na criação e na leitura seguinte.
        this.votingDeadline = votingDeadline.truncatedTo(ChronoUnit.MICROS);
        this.status = SuggestionStatus.EM_VOTACAO;
        this.createdAt = Instant.now().truncatedTo(ChronoUnit.MICROS);
    }

    protected Suggestion() {}

    public int getId() {
        return id;
    }
    public User getUser() {
        return user;
    }
    public Game getGame() {
        return game;
    }
    public int getCopies() {
        return copies;
    }
    public SuggestionStatus getStatus() {
        return status;
    }
    public Instant getVotingDeadline() {
        return votingDeadline;
    }
    public Instant getCreatedAt() {
        return createdAt;
    }

    // ---- regras de negócio (mudanças de estado só passam por aqui) ----

    public boolean votacaoAberta(Instant agora) {
        return status == SuggestionStatus.EM_VOTACAO && agora.isBefore(votingDeadline);
    }

    public boolean prazoVencido(Instant agora) {
        return !agora.isBefore(votingDeadline);
    }

    public void aprovar() {
        exigirStatus(SuggestionStatus.EM_VOTACAO);
        this.status = SuggestionStatus.APROVADA;
    }

    public void rejeitar() {
        exigirStatus(SuggestionStatus.EM_VOTACAO);
        this.status = SuggestionStatus.REJEITADA;
    }

    public void marcarComoComprada() {
        exigirStatus(SuggestionStatus.APROVADA);
        this.status = SuggestionStatus.COMPRADA;
    }

    private void exigirStatus(SuggestionStatus esperado) {
        if (this.status != esperado) {
            throw new SugestaoStatusInvalidoException(
                    "Operação inválida: a sugestão está " + this.status + " e precisaria estar " + esperado);
        }
    }
}
