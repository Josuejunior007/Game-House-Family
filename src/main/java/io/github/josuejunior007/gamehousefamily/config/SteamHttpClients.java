package io.github.josuejunior007.gamehousefamily.config;

import org.springframework.http.HttpHeaders;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

/** Cria RestClients com timeout, para a Steam fora do ar não travar a nossa API. */
public final class SteamHttpClients {

    /** A Steam responde melhor com User-Agent identificado; sem ele algumas respostas voltam vazias. */
    private static final String USER_AGENT = "GameHouseFamily/1.0 (+https://github.com/josuejunior007)";

    private SteamHttpClients() {}

    public static RestClient criar(String baseUrl) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        JdkClientHttpRequestFactory fabrica = new JdkClientHttpRequestFactory(httpClient);
        fabrica.setReadTimeout(Duration.ofSeconds(10));

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(fabrica)
                .defaultHeader(HttpHeaders.USER_AGENT, USER_AGENT)
                .build();
    }
}
