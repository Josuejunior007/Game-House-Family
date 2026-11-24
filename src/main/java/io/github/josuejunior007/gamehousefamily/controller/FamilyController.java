package io.github.josuejunior007.gamehousefamily.controller;

import io.github.josuejunior007.gamehousefamily.config.AuthInterceptor;
import io.github.josuejunior007.gamehousefamily.dto.CreateFamilyRequest;
import io.github.josuejunior007.gamehousefamily.dto.FamilyInviteResponse;
import io.github.josuejunior007.gamehousefamily.dto.FamilyResponse;
import io.github.josuejunior007.gamehousefamily.dto.InviteMemberRequest;
import io.github.josuejunior007.gamehousefamily.service.FamilyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
public class FamilyController {

    private final FamilyService familyService;

    public FamilyController(FamilyService familyService) {
        this.familyService = familyService;
    }

    /** 204 quando a pessoa ainda não está em nenhuma família (o front mostra o onboarding). */
    @GetMapping("/family")
    public ResponseEntity<FamilyResponse> minha(@SessionAttribute(AuthInterceptor.SESSION_USER_ID) Integer userId) {
        Optional<FamilyResponse> familia = familyService.minhaFamilia(userId);
        return familia.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(familia.get());
    }

    @PostMapping("/family")
    public ResponseEntity<FamilyResponse> criar(@SessionAttribute(AuthInterceptor.SESSION_USER_ID) Integer userId,
                                                @RequestBody @Valid CreateFamilyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(familyService.criar(userId, request));
    }

    @DeleteMapping("/family")
    public ResponseEntity<Void> excluir(@SessionAttribute(AuthInterceptor.SESSION_USER_ID) Integer userId) {
        familyService.excluir(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/family/leave")
    public ResponseEntity<Void> sair(@SessionAttribute(AuthInterceptor.SESSION_USER_ID) Integer userId) {
        familyService.sair(userId);
        return ResponseEntity.noContent().build();
    }

    // ---------- convites enviados (dono) ----------

    @PostMapping("/family/invites")
    public ResponseEntity<FamilyInviteResponse> convidar(
            @SessionAttribute(AuthInterceptor.SESSION_USER_ID) Integer userId,
            @RequestBody @Valid InviteMemberRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(familyService.convidar(userId, request));
    }

    @GetMapping("/family/invites")
    public List<FamilyInviteResponse> convitesEnviados(
            @SessionAttribute(AuthInterceptor.SESSION_USER_ID) Integer userId) {
        return familyService.convitesEnviados(userId);
    }

    @DeleteMapping("/family/invites/{id}")
    public ResponseEntity<Void> cancelarConvite(
            @SessionAttribute(AuthInterceptor.SESSION_USER_ID) Integer userId,
            @PathVariable Integer id) {
        familyService.cancelarConvite(userId, id);
        return ResponseEntity.noContent().build();
    }

    // ---------- membros ----------

    @DeleteMapping("/family/members/{membroUserId}")
    public ResponseEntity<Void> removerMembro(
            @SessionAttribute(AuthInterceptor.SESSION_USER_ID) Integer userId,
            @PathVariable Integer membroUserId) {
        familyService.removerMembro(userId, membroUserId);
        return ResponseEntity.noContent().build();
    }

    // ---------- convites recebidos ----------

    @GetMapping("/invites")
    public List<FamilyInviteResponse> meusConvites(
            @SessionAttribute(AuthInterceptor.SESSION_USER_ID) Integer userId) {
        return familyService.convitesRecebidos(userId);
    }

    @PostMapping("/invites/{id}/accept")
    public FamilyResponse aceitar(@SessionAttribute(AuthInterceptor.SESSION_USER_ID) Integer userId,
                                  @PathVariable Integer id) {
        return familyService.aceitar(userId, id);
    }

    @PostMapping("/invites/{id}/decline")
    public ResponseEntity<Void> recusar(@SessionAttribute(AuthInterceptor.SESSION_USER_ID) Integer userId,
                                        @PathVariable Integer id) {
        familyService.recusar(userId, id);
        return ResponseEntity.noContent().build();
    }
}
