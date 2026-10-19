package io.github.josuejunior007.gamehousefamily.service;


import io.github.josuejunior007.gamehousefamily.dto.SteamAppResponse;
import io.github.josuejunior007.gamehousefamily.dto.SteamGameData;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.Optional;

@Service
public class SteamService {

    private final RestClient restClient = RestClient.create("https://store.steampowered.com");

    public Optional<SteamGameData> buscarJogo(String appId) {

        Map<String, SteamAppResponse> resposta = restClient.get()
                .uri("/api/appdetails?appids={id}&cc=br&l=portuguese", appId)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        SteamAppResponse app = resposta.get(appId);

        if (app == null || !app.success()) {
            return Optional.empty();
        }

        return Optional.of(app.data());
    }
}