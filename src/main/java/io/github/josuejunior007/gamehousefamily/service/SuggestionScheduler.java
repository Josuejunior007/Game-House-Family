package io.github.josuejunior007.gamehousefamily.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Todo minuto fecha as votações que passaram do prazo (aprova ou rejeita pela contagem de votos). */
@Component
public class SuggestionScheduler {

    private static final Logger log = LoggerFactory.getLogger(SuggestionScheduler.class);

    private final SuggestionService suggestionService;

    public SuggestionScheduler(SuggestionService suggestionService) {
        this.suggestionService = suggestionService;
    }

    @Scheduled(fixedDelay = 60_000)
    public void encerrarVencidas() {
        int encerradas = suggestionService.encerrarVencidas();
        if (encerradas > 0) {
            log.info("{} votação(ões) encerrada(s) por prazo vencido", encerradas);
        }
    }
}
