package io.github.josuejunior007.gamehousefamily.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SteamPriceData(String currency, int initial, int Final, @JsonProperty("discount_percent") int discountPercent ) {
}
