package io.github.josuejunior007.gamehousefamily.model;

import jakarta.persistence.*;

import java.time.Instant;

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
        this.votingDeadline = votingDeadline;
        this.status = SuggestionStatus.EM_VOTACAO;
        this.createdAt = Instant.now();
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
}
