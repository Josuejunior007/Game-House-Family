package io.github.josuejunior007.gamehousefamily.service;

import io.github.josuejunior007.gamehousefamily.config.SteamHttpClients;
import io.github.josuejunior007.gamehousefamily.exception.NaoAutenticadoException;
import io.github.josuejunior007.gamehousefamily.exception.SteamIndisponivelException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Login pela Steam via OpenID 2.0.
 * 1) mandamos o usuário para a página de login da Steam (urlDeLogin);
 * 2) a Steam devolve o usuário para o nosso callback com um monte de parâmetros "openid.*";
 * 3) perguntamos de volta para a Steam se aqueles parâmetros são mesmo dela (check_authentication);
 * 4) se for, o "claimed_id" traz o SteamID64 do usuário.
 */
@Service
public class SteamOpenIdService {

    private static final String ENDPOINT = "https://steamcommunity.com/openid/login";
    private static final String NS = "http://specs.openid.net/auth/2.0";
    private static final String IDENTIFIER_SELECT = "http://specs.openid.net/auth/2.0/identifier_select";
    private static final Pattern CLAIMED_ID = Pattern.compile("^https://steamcommunity\\.com/openid/id/(\\d{17})$");

    private final RestClient client = SteamHttpClients.criar("https://steamcommunity.com");
    private final String baseUrl;

    public SteamOpenIdService(@Value("${app.base-url}") String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    public String urlDeLogin() {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("openid.ns", NS);
        params.put("openid.mode", "checkid_setup");
        params.put("openid.return_to", returnTo());
        params.put("openid.realm", baseUrl);
        params.put("openid.identity", IDENTIFIER_SELECT);
        params.put("openid.claimed_id", IDENTIFIER_SELECT);

        String query = params.entrySet().stream()
                .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
        return ENDPOINT + "?" + query;
    }

    /** Devolve o SteamID64 se a Steam confirmar o login; senão lança NaoAutenticadoException. */
    public String validar(Map<String, String[]> params) {
        if (!"id_res".equals(primeiro(params, "openid.mode"))) {
            throw new NaoAutenticadoException("Login cancelado ou inválido.");
        }
        if (!returnTo().equals(primeiro(params, "openid.return_to"))) {
            throw new NaoAutenticadoException("Login inválido (return_to diferente do esperado).");
        }
        String claimedId = primeiro(params, "openid.claimed_id");
        Matcher matcher = CLAIMED_ID.matcher(claimedId == null ? "" : claimedId);
        if (!matcher.matches()) {
            throw new NaoAutenticadoException("Login inválido (identificador Steam inesperado).");
        }

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        params.forEach((chave, valores) -> {
            if (chave.startsWith("openid.") && valores.length > 0) {
                form.add(chave, valores[0]);
            }
        });
        form.set("openid.mode", "check_authentication");

        String resposta;
        try {
            resposta = client.post()
                    .uri("/openid/login")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(String.class);
        } catch (RestClientException e) {
            throw new SteamIndisponivelException("Não foi possível confirmar o login com a Steam agora.");
        }

        if (resposta == null || !resposta.contains("is_valid:true")) {
            throw new NaoAutenticadoException("A Steam não confirmou o login.");
        }
        return matcher.group(1);
    }

    private String returnTo() {
        return baseUrl + "/auth/steam/callback";
    }

    private String primeiro(Map<String, String[]> params, String chave) {
        String[] valores = params.get(chave);
        return (valores == null || valores.length == 0) ? null : valores[0];
    }
}
