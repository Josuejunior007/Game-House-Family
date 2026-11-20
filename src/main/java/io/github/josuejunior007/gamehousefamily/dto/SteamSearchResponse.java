package io.github.josuejunior007.gamehousefamily.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Espelha o JSON de https://store.steampowered.com/api/storesearch/ (detalhe da Steam, não é exposto direto).
 * A busca também devolve bundles/pacotes além de apps, por isso o campo {@code type}.
 */
public record SteamSearchResponse(int total, List<SteamSearchItem> items) {

    public record SteamSearchItem(int id, String name, String type,
                                  @JsonProperty("tiny_image") String tinyImage,
                                  Price price) {

        public record Price(String currency, Integer initial, @JsonProperty("final") Integer finalPrice) {
        }
    }
}
