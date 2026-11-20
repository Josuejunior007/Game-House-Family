package io.github.josuejunior007.gamehousefamily.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SteamGameData(String name,
                            @JsonProperty("steam_appid") int steamAppId,
                            @JsonProperty("header_image") String headerImage,
                            @JsonProperty("price_overview") SteamPriceData priceOverview) {
}
