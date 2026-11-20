package io.github.josuejunior007.gamehousefamily.service;

import io.github.josuejunior007.gamehousefamily.exception.SugestaoNaoEncontradaException;
import io.github.josuejunior007.gamehousefamily.exception.VotacaoEncerradaException;
import io.github.josuejunior007.gamehousefamily.model.Game;
import io.github.josuejunior007.gamehousefamily.model.Suggestion;
import io.github.josuejunior007.gamehousefamily.model.User;
import io.github.josuejunior007.gamehousefamily.model.Vote;
import io.github.josuejunior007.gamehousefamily.model.VoteChoice;
import io.github.josuejunior007.gamehousefamily.repository.SuggestionRepository;
import io.github.josuejunior007.gamehousefamily.repository.UserRepository;
import io.github.josuejunior007.gamehousefamily.repository.VoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VoteServiceTest {

    @Mock VoteRepository voteRepository;
    @Mock SuggestionRepository suggestionRepository;
    @Mock UserRepository userRepository;

    VoteService service;
    User votante;
    Suggestion aberta;

    @BeforeEach
    void setUp() {
        service = new VoteService(voteRepository, suggestionRepository, userRepository);
        votante = new User("76561198000000002", "Beto", null);
        User criador = new User("76561198000000001", "Ana", null);
        aberta = new Suggestion(criador, new Game("620", "Portal 2"), 1, Instant.now().plusSeconds(3600));
    }

    @Test
    void primeiroVotoEhCriado() {
        when(suggestionRepository.findById(10)).thenReturn(Optional.of(aberta));
        when(userRepository.findById(3)).thenReturn(Optional.of(votante));
        when(voteRepository.findByUserIdAndSuggestionId(3, 10)).thenReturn(Optional.empty());
        when(voteRepository.save(any(Vote.class))).thenAnswer(inv -> inv.getArgument(0));

        Vote voto = service.votar(3, 10, VoteChoice.APOIO);

        assertThat(voto.getChoice()).isEqualTo(VoteChoice.APOIO);
        verify(voteRepository).save(any(Vote.class));
    }

    @Test
    void votarDeNovoTrocaAEscolhaSemCriarOutroVoto() {
        Vote existente = new Vote(votante, aberta, VoteChoice.APOIO);
        when(suggestionRepository.findById(10)).thenReturn(Optional.of(aberta));
        when(userRepository.findById(3)).thenReturn(Optional.of(votante));
        when(voteRepository.findByUserIdAndSuggestionId(3, 10)).thenReturn(Optional.of(existente));

        Vote voto = service.votar(3, 10, VoteChoice.NAO_APOIO);

        assertThat(voto.getChoice()).isEqualTo(VoteChoice.NAO_APOIO);
        verify(voteRepository, never()).save(any());
    }

    @Test
    void naoVotaEmVotacaoComPrazoVencido() {
        Suggestion vencida = new Suggestion(votante, new Game("620", "Portal 2"), 1, Instant.now().minusSeconds(5));
        when(suggestionRepository.findById(10)).thenReturn(Optional.of(vencida));

        assertThrows(VotacaoEncerradaException.class, () -> service.votar(3, 10, VoteChoice.APOIO));
        verify(voteRepository, never()).save(any());
    }

    @Test
    void naoVotaEmVotacaoJaEncerrada() {
        aberta.rejeitar();
        when(suggestionRepository.findById(10)).thenReturn(Optional.of(aberta));

        assertThrows(VotacaoEncerradaException.class, () -> service.votar(3, 10, VoteChoice.APOIO));
    }

    @Test
    void sugestaoInexistente() {
        when(suggestionRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(SugestaoNaoEncontradaException.class, () -> service.votar(3, 99, VoteChoice.APOIO));
    }
}
