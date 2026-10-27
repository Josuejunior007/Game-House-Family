package io.github.josuejunior007.gamehousefamily.model;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int id;
    @Column(unique = true, name = "steam_id", nullable = false)
    private String steamId;
    @Column(nullable = false, name = "display_name")
    private String displayName;
    @Column(name = "avatr_url")
    private String avatarUrl;

    public User(String steamId, String DisplayName, String avatarUrl) {
        this.steamId = steamId;
        this.displayName = DisplayName;
        this.avatarUrl = avatarUrl;

    }

    protected User() {}

    public String getDisplayName() {
        return displayName;
    }
    public int getId() {
        return id;
    }
    public String getSteamId() {
        return steamId;
    }
    public String getAvatarUrl() {
        return avatarUrl;
    }
    public void atualizarNomeAvatar(String name, String avatar) {
        this.avatarUrl = avatar;
        this.displayName = name;
    }
}


