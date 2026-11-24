package io.github.josuejunior007.gamehousefamily.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * A família do app. Neste projeto existe no máximo uma (o app é de uma família só),
 * por isso não há campo de "código" nem lista de famílias para escolher.
 */
@Entity
@Table(name = "family")
public class Family {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int id;

    @Column(nullable = false, length = 60)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public Family(String name, User owner) {
        this.name = name;
        this.owner = owner;
        this.createdAt = Instant.now().truncatedTo(ChronoUnit.MICROS);
    }

    protected Family() {}

    public int getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public User getOwner() {
        return owner;
    }
    public Instant getCreatedAt() {
        return createdAt;
    }

    public void renomear(String novoNome) {
        this.name = novoNome;
    }
}
