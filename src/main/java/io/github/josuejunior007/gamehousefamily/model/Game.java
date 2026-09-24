package io.github.josuejunior007.gamehousefamily.model;

public class Game {

    private int id;
    private String steamAppId;
    private String name;

    public Game (int id, String steamAppId, String name) {
        this.id = id;
        this.steamAppId = steamAppId;
        this.name = name;
    }
}
