package io.github.josuejunior007.gamehousefamily.dto;

import io.github.josuejunior007.gamehousefamily.model.Game;

public record GameResponse(Integer id, String steamAppId, String name, String imageUrl, String storeUrl) {

    public static GameResponse de(Game game) {

        return new GameResponse(game.getId(), game.getSteamAppId(), game.getName(),
                SteamLinks.imagemDoJogo(game), SteamLinks.loja(game.getSteamAppId()));
    }
}
