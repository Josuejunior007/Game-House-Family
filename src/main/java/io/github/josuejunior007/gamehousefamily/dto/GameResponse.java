package io.github.josuejunior007.gamehousefamily.dto;

import io.github.josuejunior007.gamehousefamily.model.Game;

public record GameResponse(Integer id, String steamAppId, String name) {

    public static GameResponse de(Game game) {

        return new GameResponse(game.getId(), game.getSteamAppId(), game.getName());
    }
}
