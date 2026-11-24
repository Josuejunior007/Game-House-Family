package io.github.josuejunior007.gamehousefamily.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final String frontendUrl;
    private final FamilyInterceptor familyInterceptor;

    public WebConfig(@Value("${app.frontend-url:}") String frontendUrl, FamilyInterceptor familyInterceptor) {
        this.frontendUrl = FrontendUrl.resolver(frontendUrl);
        this.familyInterceptor = familyInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthInterceptor())
                .order(1)
                .addPathPatterns("/**")
                .excludePathPatterns("/", "/error", "/auth/steam/**", "/auth/dev-login", "/auth/logout");

        // Só depois do login: o app (jogos, catálogo e sugestões) é da família.
        registry.addInterceptor(familyInterceptor)
                .order(2)
                .addPathPatterns("/games", "/games/**", "/suggestions", "/suggestions/**", "/steam", "/steam/**");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(origensPermitidas())
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowCredentials(true);
    }

    /**
     * Para o navegador, "localhost" e "127.0.0.1" são origens diferentes — liberar as duas evita
     * o CORS recusar o front só porque alguém abriu o atalho com o outro nome.
     */
    private String[] origensPermitidas() {
        if (frontendUrl.contains("//localhost:")) {
            return new String[] { frontendUrl, frontendUrl.replace("//localhost:", "//127.0.0.1:") };
        }
        if (frontendUrl.contains("//127.0.0.1:")) {
            return new String[] { frontendUrl, frontendUrl.replace("//127.0.0.1:", "//localhost:") };
        }
        return new String[] { frontendUrl };
    }
}
