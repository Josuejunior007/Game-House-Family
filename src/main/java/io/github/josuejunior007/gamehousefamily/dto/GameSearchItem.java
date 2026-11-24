package io.github.josuejunior007.gamehousefamily.dto;

import io.github.josuejunior007.gamehousefamily.model.Game;

/**
 * Item de busca de jogos. Vem do banco (source CATALOGO) ou da Steam (source STEAM, já salvo
 * no banco antes de responder). Sempre tem gameId, então serve direto para criar sugestão.
 */
public record GameSearchItem(Integer gameId, int steamAppId, String name, String imageUrl, String storeUrl,
                             boolean available, String currency, Integer initial, Integer finalPrice,
                             Integer discountPercent, String source) {

    public static GameSearchItem doBanco(Game jogo) {
        int appId;
        try {
            appId = Integer.parseInt(jogo.getSteamAppId());
        } catch (NumberFormatException e) {
            appId = 0; // cadastro manual fora do padrão numérico não pode derrubar a busca
        }
        return new GameSearchItem(jogo.getId(), appId, jogo.getName(),
                SteamLinks.imagemDoJogo(jogo), SteamLinks.loja(jogo.getSteamAppId()),
                false, null, null, null, null, "CATALOGO");
    }

    public static GameSearchItem daSteam(Game salvo, SteamSearchResult achado) {
        return new GameSearchItem(salvo.getId(), achado.steamAppId(), salvo.getName(),
                SteamLinks.imagemDoJogo(salvo), SteamLinks.loja(salvo.getSteamAppId()),
                achado.available(), achado.currency(), achado.initial(), achado.finalPrice(),
                achado.discountPercent(), "STEAM");
    }
}
