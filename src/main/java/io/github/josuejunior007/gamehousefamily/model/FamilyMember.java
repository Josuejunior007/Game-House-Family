package io.github.josuejunior007.gamehousefamily.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Vínculo entre um usuário e a família. O unique em user_id é o que garante a regra
 * "cada pessoa participa de uma família por vez" — inclusive contra duas requisições
 * simultâneas aceitando convites diferentes.
 */
@Entity
@Table(name = "family_member")
public class FamilyMember {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "family_id", nullable = false)
    private Family family;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private FamilyRole role;

    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt;

    public FamilyMember(Family family, User user, FamilyRole role) {
        this.family = family;
        this.user = user;
        this.role = role;
        this.joinedAt = Instant.now().truncatedTo(ChronoUnit.MICROS);
    }

    protected FamilyMember() {}

    public int getId() {
        return id;
    }
    public Family getFamily() {
        return family;
    }
    public User getUser() {
        return user;
    }
    public FamilyRole getRole() {
        return role;
    }
    public Instant getJoinedAt() {
        return joinedAt;
    }
    public boolean ehDono() {
        return role == FamilyRole.DONO;
    }
}
