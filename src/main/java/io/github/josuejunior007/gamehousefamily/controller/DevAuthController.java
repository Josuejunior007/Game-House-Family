package io.github.josuejunior007.gamehousefamily.controller;

import io.github.josuejunior007.gamehousefamily.config.SessaoUsuario;
import io.github.josuejunior007.gamehousefamily.dto.DevLoginRequest;
import io.github.josuejunior007.gamehousefamily.dto.UserResponse;
import io.github.josuejunior007.gamehousefamily.model.User;
import io.github.josuejunior007.gamehousefamily.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Login falso para testar a API sem passar pela Steam (requests.http, demonstração).
 * Só existe se app.dev-login-enabled=true. NUNCA ligar em produção.
 */
@RestController
@ConditionalOnProperty(name = "app.dev-login-enabled", havingValue = "true")
public class DevAuthController {

    private final UserService userService;

    public DevAuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/auth/dev-login")
    public UserResponse devLogin(HttpServletRequest request, @RequestBody @Valid DevLoginRequest body) {
        User user = userService.entrarDev(body.steamId(), body.displayName());
        SessaoUsuario.iniciar(request, user.getId());
        return UserResponse.de(user);
    }
}
