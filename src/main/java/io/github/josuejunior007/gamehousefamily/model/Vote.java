package io.github.josuejunior007.gamehousefamily.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "vote",
        uniqueConstraints = @UniqueConstraint(name = "uk_vote_user_suggestion", columnNames = {"user_id", "suggestion_id"}))
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "suggestion_id", nullable = false)
    private Suggestion suggestion;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VoteChoice choice;
    @Column(nullable = false)
    private Instant createdAt;
    @Column(nullable = false)
    private Instant updatedAt;

    public Vote(User user, Suggestion suggestion, VoteChoice choice) {
        this.user = user;
        this.suggestion = suggestion;
        this.choice = choice;
        this.createdAt = Instant.now().truncatedTo(ChronoUnit.MICROS);
        this.updatedAt = this.createdAt;
    }

    protected Vote() {}

    public int getId() {
        return id;
    }
    public User getUser() {
        return user;
    }
    public Suggestion getSuggestion() {
        return suggestion;
    }
    public VoteChoice getChoice() {
        return choice;
    }
    public Instant getCreatedAt() {
        return createdAt;
    }
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void alterarEscolha(VoteChoice novaEscolha) {
        this.choice = novaEscolha;
        this.updatedAt = Instant.now().truncatedTo(ChronoUnit.MICROS);
    }
}
