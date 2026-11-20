package io.github.josuejunior007.gamehousefamily.dto;

import io.github.josuejunior007.gamehousefamily.model.Game;

/** Monta links públicos da Steam a partir do appId (sem chamar a API). */
final class SteamLinks {

    private SteamLinks() {}

    static String imagem(String steamAppId) {
        return "https://cdn.cloudflare.steamstatic.com/steam/apps/" + steamAppId + "/header.jpg";
    }

    static String loja(String steamAppId) {
        return "https://store.steampowered.com/app/" + steamAppId;
    }

    /** Prefere a capa que a Steam informou no cadastro; a URL montada é só fallback de jogos antigos. */
    static String imagemDoJogo(Game game) {
        String salva = game.getImageUrl();
        return (salva == null || salva.isBlank()) ? imagem(game.getSteamAppId()) : salva;
    }
}
