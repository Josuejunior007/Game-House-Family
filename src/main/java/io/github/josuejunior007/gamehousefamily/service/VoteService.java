package io.github.josuejunior007.gamehousefamily.service;

import io.github.josuejunior007.gamehousefamily.exception.SugestaoNaoEncontradaException;
import io.github.josuejunior007.gamehousefamily.exception.UserIdNaoEncontradoNoBancoException;
import io.github.josuejunior007.gamehousefamily.exception.VotacaoEncerradaException;
import io.github.josuejunior007.gamehousefamily.model.Suggestion;
import io.github.josuejunior007.gamehousefamily.model.User;
import io.github.josuejunior007.gamehousefamily.model.Vote;
import io.github.josuejunior007.gamehousefamily.model.VoteChoice;
import io.github.josuejunior007.gamehousefamily.repository.SuggestionRepository;
import io.github.josuejunior007.gamehousefamily.repository.UserRepository;
import io.github.josuejunior007.gamehousefamily.repository.VoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
public class VoteService {

    private final VoteRepository voteRepository;
    private final SuggestionRepository suggestionRepository;
    private final UserRepository userRepository;

    public VoteService(VoteRepository voteRepository, SuggestionRepository suggestionRepository,
                       UserRepository userRepository) {
        this.voteRepository = voteRepository;
        this.suggestionRepository = suggestionRepository;
        this.userRepository = userRepository;
    }

    /**
     * RN-12 e RN-13: um voto por usuário por sugestão; votar de novo troca a escolha (enquanto a votação está aberta).
     * Se duas requisições do mesmo usuário chegarem juntas, a unique constraint (user_id, suggestion_id) do banco barra a segunda.
     */
    @Transactional
    public Vote votar(int userId, int suggestionId, VoteChoice escolha) {
        Suggestion suggestion = suggestionRepository.findById(suggestionId)
                .orElseThrow(() -> new SugestaoNaoEncontradaException("Sugestão não encontrada: " + suggestionId));

        if (!suggestion.votacaoAberta(Instant.now())) {
            throw new VotacaoEncerradaException("A votação desta sugestão já foi encerrada");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserIdNaoEncontradoNoBancoException("Usuário não encontrado: " + userId));

        Optional<Vote> existente = voteRepository.findByUserIdAndSuggestionId(userId, suggestionId);
        if (existente.isPresent()) {
            Vote voto = existente.get();
            voto.alterarEscolha(escolha); // entidade gerenciada: o JPA salva a alteração ao fim da transação
            return voto;
        }

        return voteRepository.save(new Vote(user, suggestion, escolha));
    }
}
