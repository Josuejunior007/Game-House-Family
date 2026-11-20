package io.github.josuejunior007.gamehousefamily.service;

import io.github.josuejunior007.gamehousefamily.config.SteamHttpClients;
import io.github.josuejunior007.gamehousefamily.dto.SteamAppResponse;
import io.github.josuejunior007.gamehousefamily.dto.SteamGameData;
import io.github.josuejunior007.gamehousefamily.dto.SteamPerfil;
import io.github.josuejunior007.gamehousefamily.dto.SteamSearchResponse;
import io.github.josuejunior007.gamehousefamily.dto.SteamSearchResult;
import io.github.josuejunior007.gamehousefamily.exception.SteamIndisponivelException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SteamService {

    private final RestClient restClient = SteamHttpClients.criar("https://store.steampowered.com");
    private final RestClient comunidadeClient = SteamHttpClients.criar("https://steamcommunity.com");

    public Optional<SteamGameData> buscarJogo(String appId) {

        Map<String, SteamAppResponse> resposta;
        try {
            resposta = restClient.get()
                    .uri("/api/appdetails?appids={id}&cc=br&l=portuguese", appId)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
        } catch (RestClientException e) {
            throw new SteamIndisponivelException("Não foi possível consultar a Steam agora. Tente novamente em instantes.");
        }

        if (resposta == null) {
            return Optional.empty();
        }

        SteamAppResponse app = resposta.get(appId);

        if (app == null || !app.success() || app.data() == null) {
            return Optional.empty();
        }

        return Optional.of(app.data());
    }

    /**
     * Busca jogos por nome na loja da Steam (para o usuário não precisar saber o appId).
     * Devolve só apps: a busca também retorna bundles e pacotes, que não servem para cadastrar.
     */
    public List<SteamSearchResult> buscarJogos(String termo, int limite) {
        if (termo == null || termo.isBlank()) {
            return List.of();
        }

        String busca = termo.trim();
        if (busca.length() > 100) { // a Steam não precisa mais que isso
            busca = busca.substring(0, 100);
        }
        final String parametro = busca;
        final int maximo = Math.max(1, limite);

        SteamSearchResponse resposta;
        try {
            resposta = restClient.get()
                    .uri(construir -> construir.path("/api/storesearch/")
                            .queryParam("term", parametro)
                            .queryParam("l", "portuguese")
                            .queryParam("cc", "br")
                            .build())
                    .retrieve()
                    .body(SteamSearchResponse.class);
        } catch (RestClientException e) {
            throw new SteamIndisponivelException(
                    "Não foi possível buscar jogos na Steam agora. Tente novamente em instantes.");
        }

        if (resposta == null || resposta.items() == null) {
            return List.of();
        }

        return resposta.items().stream()
                .filter(item -> "app".equals(item.type()))
                .filter(item -> item.id() > 0 && item.name() != null)
                .limit(maximo)
                .map(SteamSearchResult::de)
                .toList();
    }

    /**
     * Busca nome e avatar no perfil público (XML), sem precisar de chave de API.
     * Se der qualquer problema (perfil privado, Steam fora), devolve um nome genérico: o login não deve falhar por isso.
     */
    public SteamPerfil buscarPerfil(String steamId) {
        String padrao = "Jogador " + steamId.substring(steamId.length() - 4);
        try {
            String xml = comunidadeClient.get()
                    .uri("/profiles/{id}?xml=1", steamId)
                    .retrieve()
                    .body(String.class);
            if (xml == null || xml.isBlank()) {
                return new SteamPerfil(padrao, null);
            }

            DocumentBuilderFactory fabrica = DocumentBuilderFactory.newInstance();
            fabrica.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true); // protege contra XXE
            fabrica.setXIncludeAware(false);
            fabrica.setExpandEntityReferences(false);
            Document doc = fabrica.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));

            String nome = textoDoNo(doc, "steamID");
            String avatar = textoDoNo(doc, "avatarFull");
            return new SteamPerfil(nome != null ? nome : padrao, avatar);
        } catch (RestClientException | ParserConfigurationException | SAXException | IOException e) {
            return new SteamPerfil(padrao, null);
        }
    }

    private String textoDoNo(Document doc, String tag) {
        NodeList nos = doc.getElementsByTagName(tag);
        if (nos.getLength() == 0) {
            return null;
        }
        String texto = nos.item(0).getTextContent();
        return (texto == null || texto.isBlank()) ? null : texto.trim();
    }
}
