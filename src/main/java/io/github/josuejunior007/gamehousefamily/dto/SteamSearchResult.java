package io.github.josuejunior007.gamehousefamily.dto;

/**
 * Item de busca já pronto para o front-end: o usuário clica no jogo e manda o
 * {@code steamAppId} para {@code POST /games}.
 */
public record SteamSearchResult(int steamAppId, String name, String imageUrl, String storeUrl,
                                boolean available, String currency, Integer initial, Integer finalPrice,
                                Integer discountPercent) {

    public static SteamSearchResult de(SteamSearchResponse.SteamSearchItem item) {
        String appId = String.valueOf(item.id());
        // A própria busca já devolve a miniatura certa — usar a URL montada deixava jogos sem capa.
        String imagem = (item.tinyImage() == null || item.tinyImage().isBlank())
                ? SteamLinks.imagem(appId)
                : item.tinyImage();
        SteamSearchResponse.SteamSearchItem.Price preco = item.price();
        if (preco == null) { // jogo gratuito, ainda não lançado ou indisponível na região
            return new SteamSearchResult(item.id(), item.name(), imagem, SteamLinks.loja(appId),
                    false, null, null, null, null);
        }

        Integer inicial = preco.initial();
        Integer atual = preco.finalPrice();
        Integer desconto = (inicial != null && atual != null && inicial > 0)
                ? (int) Math.round((inicial - atual) * 100.0 / inicial)
                : 0;

        return new SteamSearchResult(item.id(), item.name(), imagem, SteamLinks.loja(appId),
                true, preco.currency(), inicial, atual, desconto);
    }
}
