package io.github.josuejunior007.gamehousefamily.controller;

import io.github.josuejunior007.gamehousefamily.dto.SteamSearchResult;
import io.github.josuejunior007.gamehousefamily.service.SteamService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Ponte para a busca da loja da Steam. Existe para o usuário não ter de descobrir o appId
 * numérico na mão: ele digita o nome, escolhe o jogo e o front manda o appId para POST /games.
 */
@RestController
public class SteamController {

    private static final int LIMITE_RESULTADOS = 10;

    private final SteamService steamService;

    public SteamController(SteamService steamService) {
        this.steamService = steamService;
    }

    @GetMapping("/steam/search")
    public List<SteamSearchResult> buscar(@RequestParam(name = "q") String termo) {
        return steamService.buscarJogos(termo, LIMITE_RESULTADOS);
    }
}
