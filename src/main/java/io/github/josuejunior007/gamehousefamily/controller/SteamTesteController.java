package io.github.josuejunior007.gamehousefamily.controller;

import io.github.josuejunior007.gamehousefamily.dto.SteamGameData;
import io.github.josuejunior007.gamehousefamily.service.SteamService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SteamTesteController {

    private final SteamService steamService;

    public SteamTesteController(SteamService steamService) {
        this.steamService = steamService;
    }

    @GetMapping("/steam/{appId}")
    public ResponseEntity<SteamGameData> buscar(@PathVariable String appId) {
        return ResponseEntity.of(steamService.buscarJogo(appId));
    }
}
