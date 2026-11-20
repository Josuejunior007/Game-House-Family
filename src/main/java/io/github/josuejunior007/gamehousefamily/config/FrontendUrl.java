package io.github.josuejunior007.gamehousefamily.config;

/**
 * Resolve a URL do front-end. {@code APP_FRONTEND_URL} ausente <b>ou vazia</b> cai no padrão de
 * desenvolvimento — e isso importa: sem URL de front o CORS não é registrado e o callback do login
 * Steam devolve JSON em vez de voltar para a aplicação, um sintoma que parece "login quebrado".
 */
public final class FrontendUrl {

    public static final String PADRAO_DESENVOLVIMENTO = "http://localhost:5173";

    private FrontendUrl() {}

    public static String resolver(String configurada) {
        return (configurada == null || configurada.isBlank()) ? PADRAO_DESENVOLVIMENTO : configurada.trim();
    }
}
