package io.github.josuejunior007.gamehousefamily.config;

import io.github.josuejunior007.gamehousefamily.exception.NaoAutenticadoException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

/** Barra qualquer requisição sem sessão de login (exceto as rotas liberadas no WebConfig). */
public class AuthInterceptor implements HandlerInterceptor {

    public static final String SESSION_USER_ID = "userId";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) { // pré-flight do CORS
            return true;
        }
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(SESSION_USER_ID) == null) {
            throw new NaoAutenticadoException("Faça login com a Steam em /auth/steam/login");
        }
        return true;
    }
}
