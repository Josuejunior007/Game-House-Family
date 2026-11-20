package io.github.josuejunior007.gamehousefamily.service;

import io.github.josuejunior007.gamehousefamily.dto.SteamPerfil;
import io.github.josuejunior007.gamehousefamily.exception.AcessoNegadoException;
import io.github.josuejunior007.gamehousefamily.model.User;
import io.github.josuejunior007.gamehousefamily.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final SteamService steamService;
    private final List<String> steamIdsPermitidos;

    public UserService(UserRepository userRepository, SteamService steamService,
                       @Value("${app.allowed-steam-ids:}") List<String> steamIdsPermitidos) {
        this.userRepository = userRepository;
        this.steamService = steamService;
        this.steamIdsPermitidos = steamIdsPermitidos.stream()
                .map(String::trim)
                .filter(id -> !id.isBlank())
                .toList();
    }

    /** Chamado depois que a Steam confirmou a identidade. Cria o usuário no 1º login e atualiza nome/avatar nos próximos. */
    @Transactional
    public User entrarComSteam(String steamId) {
        // lista vazia = qualquer conta Steam entra; preenchida = só a família
        if (!steamIdsPermitidos.isEmpty() && !steamIdsPermitidos.contains(steamId)) {
            throw new AcessoNegadoException("Esta conta Steam não faz parte da família.");
        }
        SteamPerfil perfil = steamService.buscarPerfil(steamId);
        return salvarOuAtualizar(steamId, perfil.displayName(), perfil.avatarUrl());
    }

    /** Só para desenvolvimento/testes (controller condicionado a uma propriedade). Não chama a Steam. */
    @Transactional
    public User entrarDev(String steamId, String displayName) {
        return salvarOuAtualizar(steamId, displayName, null);
    }

    @Transactional(readOnly = true)
    public Optional<User> buscarPorId(Integer id) {
        return userRepository.findById(id);
    }

    private User salvarOuAtualizar(String steamId, String nome, String avatar) {
        Optional<User> existente = userRepository.findBySteamId(steamId);
        if (existente.isPresent()) {
            User user = existente.get();
            user.atualizarNomeAvatar(nome, avatar);
            return user;
        }
        return userRepository.save(new User(steamId, nome, avatar));
    }
}
