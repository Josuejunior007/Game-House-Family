package io.github.josuejunior007.gamehousefamily.dto;

/** Preço consultado na Steam na hora (não é salvo no banco). Valores em centavos (4760 = R$ 47,60). */
public record GamePriceResponse(boolean available, String currency, Integer initial, Integer finalPrice,
                                Integer discountPercent) {

    public static GamePriceResponse de(SteamGameData dados) {
        SteamPriceData preco = dados.priceOverview();
        if (preco == null) { // jogo gratuito, não lançado ou indisponível na região
            return new GamePriceResponse(false, null, null, null, null);
        }
        return new GamePriceResponse(true, preco.currency(), preco.initial(), preco.finalPrice(),
                preco.discountPercent());
    }
}
