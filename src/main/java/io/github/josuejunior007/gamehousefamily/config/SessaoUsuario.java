package io.github.josuejunior007.gamehousefamily.config;

import jakarta.servlet.http.HttpServletRequest;

public final class SessaoUsuario {

    private SessaoUsuario() {}

    public static void iniciar(HttpServletRequest request, int userId) {
        request.getSession(true);
        request.changeSessionId(); // novo id de sessão após login (evita session fixation)
        request.getSession().setAttribute(AuthInterceptor.SESSION_USER_ID, userId);
    }
}
