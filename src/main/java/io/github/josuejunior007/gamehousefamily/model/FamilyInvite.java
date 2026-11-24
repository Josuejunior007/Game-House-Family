package io.github.josuejunior007.gamehousefamily.model;

import io.github.josuejunior007.gamehousefamily.exception.ConviteInvalidoException;
import jakarta.persistence.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Convite do dono para um SteamID64. O convite é feito pelo ID (não pelo usuário do banco)
 * porque a pessoa pode nunca ter entrado no app: quando ela entrar com a Steam, o convite
 * aparece na aba "Convites" e é aceito ali.
 */
@Entity
@Table(name = "family_invite", indexes = {
        @Index(name = "ix_family_invite_steam_status", columnList = "steam_id,status"),
        @Index(name = "ix_family_invite_family_status", columnList = "family_id,status")
})
public class FamilyInvite {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "family_id", nullable = false)
    private Family family;

    @Column(name = "steam_id", nullable = false, length = 20)
    private String steamId;

    /** Nome de quem foi convidado, se a conta já existia no banco. Pode ser nulo (nunca entrou). */
    @Column(name = "invited_name", length = 255)
    private String invitedName;

    @Column(name = "invited_avatar_url", length = 500)
    private String invitedAvatarUrl;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invited_by", nullable = false)
    private User invitedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private InviteStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "responded_at")
    private Instant respondedAt;

    public FamilyInvite(Family family, String steamId, String invitedName, String invitedAvatarUrl, User invitedBy) {
        this.family = family;
        this.steamId = steamId;
        this.invitedName = invitedName;
        this.invitedAvatarUrl = invitedAvatarUrl;
        this.invitedBy = invitedBy;
        this.status = InviteStatus.PENDENTE;
        this.createdAt = Instant.now().truncatedTo(ChronoUnit.MICROS);
    }

    protected FamilyInvite() {}

    public int getId() {
        return id;
    }
    public Family getFamily() {
        return family;
    }
    public String getSteamId() {
        return steamId;
    }
    public String getInvitedName() {
        return invitedName;
    }
    public String getInvitedAvatarUrl() {
        return invitedAvatarUrl;
    }
    public User getInvitedBy() {
        return invitedBy;
    }
    public InviteStatus getStatus() {
        return status;
    }
    public Instant getCreatedAt() {
        return createdAt;
    }
    public Instant getRespondedAt() {
        return respondedAt;
    }

    // ---- regras: convite só muda de estado enquanto está pendente ----

    public void aceitar() {
        exigirPendente();
        this.status = InviteStatus.ACEITO;
        this.respondedAt = Instant.now().truncatedTo(ChronoUnit.MICROS);
    }

    public void recusar() {
        exigirPendente();
        this.status = InviteStatus.RECUSADO;
        this.respondedAt = Instant.now().truncatedTo(ChronoUnit.MICROS);
    }

    public void cancelar() {
        exigirPendente();
        this.status = InviteStatus.CANCELADO;
        this.respondedAt = Instant.now().truncatedTo(ChronoUnit.MICROS);
    }

    private void exigirPendente() {
        if (status != InviteStatus.PENDENTE) {
            throw new ConviteInvalidoException("Este convite não está mais pendente (status: " + status + ").");
        }
    }
}
