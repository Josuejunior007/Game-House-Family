package io.github.josuejunior007.gamehousefamily.model;

import jakarta.persistence.*;

@Entity
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int id;
    @Column(unique = true, name = "steam_app_id", nullable = false)
    private String steamAppId;
    @Column(nullable = false)
    private String name;

    public Game(String steamAppId, String name) {
        this.steamAppId = steamAppId;
        this.name = name;
    }

    protected Game() {}

    public String getName(){
        return name;
    }
    public int getId() {
        return id;
    }
    public String getSteamAppId() {
            return steamAppId;
    }
}
