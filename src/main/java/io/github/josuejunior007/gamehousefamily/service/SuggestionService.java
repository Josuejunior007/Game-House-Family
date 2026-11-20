package io.github.josuejunior007.gamehousefamily.service;

import io.github.josuejunior007.gamehousefamily.dto.SuggestionCreateRequest;
import io.github.josuejunior007.gamehousefamily.dto.SuggestionResponse;
import io.github.josuejunior007.gamehousefamily.exception.AcessoNegadoException;
import io.github.josuejunior007.gamehousefamily.exception.GameIdNaoEncontradoNoBancoException;
import io.github.josuejunior007.gamehousefamily.exception.SugestaoAtivaJaExisteException;
import io.github.josuejunior007.gamehousefamily.exception.SugestaoNaoEncontradaException;
import io.github.josuejunior007.gamehousefamily.exception.UserIdNaoEncontradoNoBancoException;
import io.github.josuejunior007.gamehousefamily.model.Game;
import io.github.josuejunior007.gamehousefamily.model.Suggestion;
import io.github.josuejunior007.gamehousefamily.model.SuggestionStatus;
import io.github.josuejunior007.gamehousefamily.model.User;
import io.github.josuejunior007.gamehousefamily.model.Vote;
import io.github.josuejunior007.gamehousefamily.model.VoteChoice;
import io.github.josuejunior007.gamehousefamily.repository.GameRepository;
import io.github.josuejunior007.gamehousefamily.repository.SuggestionRepository;
import io.github.josuejunior007.gamehousefamily.repository.UserRepository;
import io.github.josuejunior007.gamehousefamily.repository.VoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Devolve SuggestionResponse (e não a entidade) porque a resposta precisa de dados agregados
 * (contagem de votos e o voto de quem está logado), que não pertencem à entidade Suggestion.
 */
@Service
public class SuggestionService {

    private final SuggestionRepository suggestionRepository;
    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    private final VoteRepository voteRepository;

    public SuggestionService(SuggestionRepository suggestionRepository, GameRepository gameRepository,
                             UserRepository userRepository, VoteRepository voteRepository) {
        this.suggestionRepository = suggestionRepository;
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
        this.voteRepository = voteRepository;
    }

    @Transactional
    public SuggestionResponse criar(int userId, SuggestionCreateRequest request) {
        Game game = gameRepository.findById(request.gameId())
                .orElseThrow(() -> new GameIdNaoEncontradoNoBancoException("Jogo não encontrado: " + request.gameId()));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserIdNaoEncontradoNoBancoException("Usuário não encontrado: " + userId));

        // RN-04 / RN-09: só uma sugestão ativa (EM_VOTACAO) por jogo
        if (suggestionRepository.existsByGameIdAndStatus(game.getId(), SuggestionStatus.EM_VOTACAO)) {
            throw new SugestaoAtivaJaExisteException("Já existe uma sugestão em votação para este jogo");
        }

        Instant prazo = Instant.now().plus(request.votingDays(), ChronoUnit.DAYS)
                .truncatedTo(ChronoUnit.MICROS);
        Suggestion suggestion = suggestionRepository.save(new Suggestion(user, game, request.copies(), prazo));

        return montarResposta(suggestion, userId);
    }

    @Transactional(readOnly = true)
    public List<SuggestionResponse> listar(SuggestionStatus status, int usuarioLogadoId) {
        List<Suggestion> sugestoes = (status == null)
                ? suggestionRepository.findAllByOrderByCreatedAtDesc()
                : suggestionRepository.findByStatusOrderByCreatedAtDesc(status);

        return sugestoes.stream()
                .map(s -> montarResposta(s, usuarioLogadoId))
                .toList();
    }

    @Transactional(readOnly = true)
    public SuggestionResponse buscarPorId(int id, int usuarioLogadoId) {
        return montarResposta(buscarOuFalhar(id), usuarioLogadoId);
    }

    /**
     * RN-14: aprovada se APOIO > NAO_APOIO; empate ou nenhum voto = rejeitada.
     * RN-15: o criador pode encerrar a qualquer momento; os demais só depois do prazo.
     */
    @Transactional
    public SuggestionResponse encerrar(int id, int usuarioLogadoId) {
        Suggestion suggestion = buscarOuFalhar(id);

        boolean criador = suggestion.getUser().getId() == usuarioLogadoId;
        if (!criador && !suggestion.prazoVencido(Instant.now())) {
            throw new AcessoNegadoException("Só quem criou a sugestão pode encerrar a votação antes do prazo");
        }

        fechar(suggestion);
        return montarResposta(suggestion, usuarioLogadoId);
    }

    /** RN-16: qualquer membro logado pode marcar como comprada uma sugestão já aprovada. */
    @Transactional
    public SuggestionResponse marcarComoComprada(int id, int usuarioLogadoId) {
        Suggestion suggestion = buscarOuFalhar(id);
        suggestion.marcarComoComprada();
        return montarResposta(suggestion, usuarioLogadoId);
    }

    /** Chamado pelo agendador: fecha tudo que passou do prazo. Devolve quantas foram encerradas. */
    @Transactional
    public int encerrarVencidas() {
        List<Suggestion> vencidas = suggestionRepository
                .findByStatusAndVotingDeadlineBefore(SuggestionStatus.EM_VOTACAO, Instant.now());
        vencidas.forEach(this::fechar);
        return vencidas.size();
    }

    private void fechar(Suggestion suggestion) {
        long apoios = voteRepository.countBySuggestionIdAndChoice(suggestion.getId(), VoteChoice.APOIO);
        long naoApoios = voteRepository.countBySuggestionIdAndChoice(suggestion.getId(), VoteChoice.NAO_APOIO);

        if (apoios > naoApoios) {
            suggestion.aprovar();
        } else {
            suggestion.rejeitar();
        }
    }

    private Suggestion buscarOuFalhar(int id) {
        return suggestionRepository.findById(id)
                .orElseThrow(() -> new SugestaoNaoEncontradaException("Sugestão não encontrada: " + id));
    }

    private SuggestionResponse montarResposta(Suggestion suggestion, int usuarioLogadoId) {
        long apoios = voteRepository.countBySuggestionIdAndChoice(suggestion.getId(), VoteChoice.APOIO);
        long naoApoios = voteRepository.countBySuggestionIdAndChoice(suggestion.getId(), VoteChoice.NAO_APOIO);
        VoteChoice meuVoto = voteRepository.findByUserIdAndSuggestionId(usuarioLogadoId, suggestion.getId())
                .map(Vote::getChoice)
                .orElse(null);

        return SuggestionResponse.de(suggestion, apoios, naoApoios, meuVoto);
    }
}
