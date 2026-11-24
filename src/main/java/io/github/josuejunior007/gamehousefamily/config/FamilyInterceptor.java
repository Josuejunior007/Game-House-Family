package io.github.josuejunior007.gamehousefamily.config;

import io.github.josuejunior007.gamehousefamily.exception.AcessoNegadoException;
import io.github.josuejunior007.gamehousefamily.service.FamilyService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Quem está logado mas ainda não entrou em uma família só pode ver o onboarding e os convites:
 * jogos, catálogo e sugestões são da família. Sem isso, qualquer conta Steam que logasse
 * conseguiria mexer nas sugestões dos outros.
 */
@Component
public class FamilyInterceptor implements HandlerInterceptor {

    private final FamilyService familyService;

    public FamilyInterceptor(FamilyService familyService) {
        this.familyService = familyService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) { // pré-flight do CORS
            return true;
        }
        HttpSession session = request.getSession(false);
        Object userId = session == null ? null : session.getAttribute(AuthInterceptor.SESSION_USER_ID);
        if (userId == null) { // sem sessão: o AuthInterceptor responde 401
            return true;
        }
        if (!familyService.ehMembro((Integer) userId)) {
            throw new AcessoNegadoException("Você ainda não faz parte da família. Peça ao dono para adicionar o seu SteamID.");
        }
        return true;
    }
}
