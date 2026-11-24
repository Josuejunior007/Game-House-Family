package io.github.josuejunior007.gamehousefamily.controller;

import io.github.josuejunior007.gamehousefamily.config.AuthInterceptor;
import io.github.josuejunior007.gamehousefamily.config.FrontendUrl;
import io.github.josuejunior007.gamehousefamily.config.SessaoUsuario;
import io.github.josuejunior007.gamehousefamily.dto.UserResponse;
import io.github.josuejunior007.gamehousefamily.exception.UserIdNaoEncontradoNoBancoException;
import io.github.josuejunior007.gamehousefamily.model.User;
import io.github.josuejunior007.gamehousefamily.service.FamilyService;
import io.github.josuejunior007.gamehousefamily.service.SteamOpenIdService;
import io.github.josuejunior007.gamehousefamily.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.net.URI;

@RestController
public class AuthController {

    private final SteamOpenIdService openIdService;
    private final UserService userService;
    private final FamilyService familyService;
    private final String frontendUrl;

    public AuthController(SteamOpenIdService openIdService, UserService userService, FamilyService familyService,
                          @Value("${app.frontend-url:}") String frontendUrl) {
        this.openIdService = openIdService;
        this.userService = userService;
        this.familyService = familyService;
        this.frontendUrl = FrontendUrl.resolver(frontendUrl);
    }

    @GetMapping("/auth/steam/login")
    public ResponseEntity<Void> login() {
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(openIdService.urlDeLogin())).build();
    }

    /**
     * Retorno da Steam: valida o OpenID, cria/atualiza o usuário, abre a sessão e devolve o
     * usuário para o front-end. Devolver JSON aqui deixava o navegador parado numa tela de JSON,
     * dando a impressão de que o login tinha falhado.
     */
    @GetMapping("/auth/steam/callback")
    public ResponseEntity<Void> callback(HttpServletRequest request) {
        String steamId = openIdService.validar(request.getParameterMap());
        User user = userService.entrarComSteam(steamId);
        SessaoUsuario.iniciar(request, user.getId());

        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(frontendUrl)).build();
    }

    @GetMapping("/auth/me")
    public UserResponse me(@SessionAttribute(AuthInterceptor.SESSION_USER_ID) Integer userId) {
        User user = userService.buscarPorId(userId)
                .orElseThrow(() -> new UserIdNaoEncontradoNoBancoException("Usuário não encontrado: " + userId));
        return UserResponse.de(user, familyService.resumo(userId).orElse(null));
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.noContent().build();
    }
}
