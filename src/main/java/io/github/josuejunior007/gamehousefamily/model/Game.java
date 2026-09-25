package io.github.josuejunior007.gamehousefamily.model;

public class Game {

    private int id;
    private final String steamAppId;
    private String name;

    public Game (int id, String steamAppId, String name) {
        this.id = id;
        this.steamAppId = steamAppId;
        this.name = name;
    }

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
