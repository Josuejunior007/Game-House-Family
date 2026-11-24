package io.github.josuejunior007.gamehousefamily.service;

import io.github.josuejunior007.gamehousefamily.dto.SteamPerfil;
import io.github.josuejunior007.gamehousefamily.model.User;
import io.github.josuejunior007.gamehousefamily.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final SteamService steamService;

    public UserService(UserRepository userRepository, SteamService steamService) {
        this.userRepository = userRepository;
        this.steamService = steamService;
    }

    /**
     * Chamado depois que a Steam confirmou a identidade. Cria o usuário no 1º login e atualiza
     * nome/avatar nos próximos. O acesso ao app não é decidido aqui: quem não está na família
     * entra, vê o onboarding e os convites, mas o FamilyInterceptor barra o resto.
     */
    @Transactional
    public User entrarComSteam(String steamId) {
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
