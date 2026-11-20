package io.github.josuejunior007.gamehousefamily.service;

import io.github.josuejunior007.gamehousefamily.dto.SuggestionCreateRequest;
import io.github.josuejunior007.gamehousefamily.dto.SuggestionResponse;
import io.github.josuejunior007.gamehousefamily.exception.AcessoNegadoException;
import io.github.josuejunior007.gamehousefamily.exception.GameIdNaoEncontradoNoBancoException;
import io.github.josuejunior007.gamehousefamily.exception.SugestaoAtivaJaExisteException;
import io.github.josuejunior007.gamehousefamily.exception.UserIdNaoEncontradoNoBancoException;
import io.github.josuejunior007.gamehousefamily.model.Game;
import io.github.josuejunior007.gamehousefamily.model.Suggestion;
import io.github.josuejunior007.gamehousefamily.model.SuggestionStatus;
import io.github.josuejunior007.gamehousefamily.model.User;
import io.github.josuejunior007.gamehousefamily.model.VoteChoice;
import io.github.josuejunior007.gamehousefamily.repository.GameRepository;
import io.github.josuejunior007.gamehousefamily.repository.SuggestionRepository;
import io.github.josuejunior007.gamehousefamily.repository.UserRepository;
import io.github.josuejunior007.gamehousefamily.repository.VoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SuggestionServiceTest {

    @Mock SuggestionRepository suggestionRepository;
    @Mock GameRepository gameRepository;
    @Mock UserRepository userRepository;
    @Mock VoteRepository voteRepository;

    SuggestionService service;
    Game game;
    User criador;

    @BeforeEach
    void setUp() {
        service = new SuggestionService(suggestionRepository, gameRepository, userRepository, voteRepository);
        game = new Game("620", "Portal 2");
        ReflectionTestUtils.setField(game, "id", 1);
        criador = new User("76561198000000001", "Ana", null);
        ReflectionTestUtils.setField(criador, "id", 2);
    }

    // ---------- criar ----------

    @Test
    void criarSalvaComPrazoCalculadoEmDias() {
        when(gameRepository.findById(1)).thenReturn(Optional.of(game));
        when(userRepository.findById(2)).thenReturn(Optional.of(criador));
        when(suggestionRepository.existsByGameIdAndStatus(1, SuggestionStatus.EM_VOTACAO)).thenReturn(false);
        when(suggestionRepository.save(any(Suggestion.class))).thenAnswer(inv -> inv.getArgument(0));

        Instant antes = Instant.now();
        SuggestionResponse resposta = service.criar(2, new SuggestionCreateRequest(1, 2, 3));
        Instant depois = Instant.now();

        assertThat(resposta.status()).isEqualTo(SuggestionStatus.EM_VOTACAO);
        assertThat(resposta.copies()).isEqualTo(2);
        // tolerância de 1 s: o prazo é truncado em microssegundos (o Postgres não guarda mais que isso)
        assertThat(resposta.votingDeadline())
                .isBetween(antes.plus(3, ChronoUnit.DAYS).minusSeconds(1),
                        depois.plus(3, ChronoUnit.DAYS).plusSeconds(1));
        assertThat(resposta.votingDeadline().getNano() % 1000).isZero();
    }

    @Test
    void criarFalhaSeJogoNaoExiste() {
        when(gameRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(GameIdNaoEncontradoNoBancoException.class,
                () -> service.criar(2, new SuggestionCreateRequest(99, 1, 7)));
    }

    @Test
    void criarFalhaSeUsuarioNaoExiste() {
        when(gameRepository.findById(1)).thenReturn(Optional.of(game));
        when(userRepository.findById(2)).thenReturn(Optional.empty());

        assertThrows(UserIdNaoEncontradoNoBancoException.class,
                () -> service.criar(2, new SuggestionCreateRequest(1, 1, 7)));
    }

    @Test
    void criarFalhaSeJaExisteSugestaoAtivaParaOJogo() {
        when(gameRepository.findById(1)).thenReturn(Optional.of(game));
        when(userRepository.findById(2)).thenReturn(Optional.of(criador));
        when(suggestionRepository.existsByGameIdAndStatus(1, SuggestionStatus.EM_VOTACAO)).thenReturn(true);

        assertThrows(SugestaoAtivaJaExisteException.class,
                () -> service.criar(2, new SuggestionCreateRequest(1, 1, 7)));
        verify(suggestionRepository, never()).save(any());
    }

    // ---------- encerrar ----------

    private Suggestion sugestaoComPrazo(Instant prazo) {
        Suggestion s = new Suggestion(criador, game, 1, prazo);
        when(suggestionRepository.findById(10)).thenReturn(Optional.of(s));
        return s;
    }

    private void placar(long apoios, long naoApoios) {
        when(voteRepository.countBySuggestionIdAndChoice(anyInt(), eq(VoteChoice.APOIO))).thenReturn(apoios);
        when(voteRepository.countBySuggestionIdAndChoice(anyInt(), eq(VoteChoice.NAO_APOIO))).thenReturn(naoApoios);
    }

    @Test
    void encerrarAprovaQuandoApoioVence() {
        Suggestion s = sugestaoComPrazo(Instant.now().plusSeconds(3600));
        placar(2, 1);

        service.encerrar(10, 2); // 2 = criador

        assertThat(s.getStatus()).isEqualTo(SuggestionStatus.APROVADA);
    }

    @Test
    void encerrarRejeitaEmCasoDeEmpate() {
        Suggestion s = sugestaoComPrazo(Instant.now().plusSeconds(3600));
        placar(1, 1);

        service.encerrar(10, 2);

        assertThat(s.getStatus()).isEqualTo(SuggestionStatus.REJEITADA);
    }

    @Test
    void encerrarRejeitaSemNenhumVoto() {
        Suggestion s = sugestaoComPrazo(Instant.now().plusSeconds(3600));
        placar(0, 0);

        service.encerrar(10, 2);

        assertThat(s.getStatus()).isEqualTo(SuggestionStatus.REJEITADA);
    }

    @Test
    void naoCriadorNaoEncerraAntesDoPrazo() {
        Suggestion s = sugestaoComPrazo(Instant.now().plusSeconds(3600));

        assertThrows(AcessoNegadoException.class, () -> service.encerrar(10, 3)); // 3 = outro usuário
        assertThat(s.getStatus()).isEqualTo(SuggestionStatus.EM_VOTACAO);
    }

    @Test
    void qualquerUmEncerraDepoisDoPrazo() {
        Suggestion s = sugestaoComPrazo(Instant.now().minusSeconds(60));
        placar(1, 0);

        service.encerrar(10, 3);

        assertThat(s.getStatus()).isEqualTo(SuggestionStatus.APROVADA);
    }
}
