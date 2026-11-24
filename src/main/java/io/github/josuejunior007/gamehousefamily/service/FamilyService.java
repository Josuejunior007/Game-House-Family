package io.github.josuejunior007.gamehousefamily.service;

import io.github.josuejunior007.gamehousefamily.dto.CreateFamilyRequest;
import io.github.josuejunior007.gamehousefamily.dto.FamilyInviteResponse;
import io.github.josuejunior007.gamehousefamily.dto.FamilyResponse;
import io.github.josuejunior007.gamehousefamily.dto.FamilySummary;
import io.github.josuejunior007.gamehousefamily.dto.InviteMemberRequest;
import io.github.josuejunior007.gamehousefamily.exception.AcessoNegadoException;
import io.github.josuejunior007.gamehousefamily.exception.ConviteInvalidoException;
import io.github.josuejunior007.gamehousefamily.exception.ConviteNaoEncontradoException;
import io.github.josuejunior007.gamehousefamily.exception.FamiliaJaExisteException;
import io.github.josuejunior007.gamehousefamily.exception.MembroNaoEncontradoException;
import io.github.josuejunior007.gamehousefamily.exception.UserIdNaoEncontradoNoBancoException;
import io.github.josuejunior007.gamehousefamily.model.Family;
import io.github.josuejunior007.gamehousefamily.model.FamilyInvite;
import io.github.josuejunior007.gamehousefamily.model.FamilyMember;
import io.github.josuejunior007.gamehousefamily.model.FamilyRole;
import io.github.josuejunior007.gamehousefamily.model.InviteStatus;
import io.github.josuejunior007.gamehousefamily.model.User;
import io.github.josuejunior007.gamehousefamily.repository.FamilyInviteRepository;
import io.github.josuejunior007.gamehousefamily.repository.FamilyMemberRepository;
import io.github.josuejunior007.gamehousefamily.repository.FamilyRepository;
import io.github.josuejunior007.gamehousefamily.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Regras da família: existe no máximo uma; o dono é quem a criou; só o dono adiciona e remove
 * pessoas; cada pessoa participa de uma família por vez; convite é feito pelo SteamID64 e só
 * vira membro depois que a pessoa aceita na aba "Convites".
 */
@Service
public class FamilyService {

    private final FamilyRepository familyRepository;
    private final FamilyMemberRepository memberRepository;
    private final FamilyInviteRepository inviteRepository;
    private final UserRepository userRepository;
    /** Opcional: se preenchido, só esta conta Steam pode criar a família (evita um estranho virar dono). */
    private final String donoSteamIdConfigurado;

    public FamilyService(FamilyRepository familyRepository, FamilyMemberRepository memberRepository,
                         FamilyInviteRepository inviteRepository, UserRepository userRepository,
                         @Value("${app.family-owner-steam-id:}") String donoSteamIdConfigurado) {
        this.familyRepository = familyRepository;
        this.memberRepository = memberRepository;
        this.inviteRepository = inviteRepository;
        this.userRepository = userRepository;
        this.donoSteamIdConfigurado = donoSteamIdConfigurado == null ? "" : donoSteamIdConfigurado.trim();
    }

    // ---------- leitura ----------

    @Transactional(readOnly = true)
    public Optional<FamilyResponse> minhaFamilia(int userId) {
        return memberRepository.findByUserId(userId).map(membro -> montar(membro.getFamily()));
    }

    /** Resumo usado no /auth/me (evita uma segunda chamada só para saber a família). */
    @Transactional(readOnly = true)
    public Optional<FamilySummary> resumo(int userId) {
        return memberRepository.findByUserId(userId).map(membro ->
                new FamilySummary(membro.getFamily().getId(), membro.getFamily().getName(), membro.getRole()));
    }

    @Transactional(readOnly = true)
    public boolean ehMembro(int userId) {
        return memberRepository.existsByUserId(userId);
    }

    @Transactional(readOnly = true)
    public boolean ehDono(int userId) {
        return memberRepository.findByUserId(userId).map(FamilyMember::ehDono).orElse(false);
    }

    // ---------- família ----------

    @Transactional
    public FamilyResponse criar(int userId, CreateFamilyRequest request) {
        User user = usuario(userId);

        if (familyRepository.count() > 0) {
            throw new FamiliaJaExisteException(
                    "Este app já tem uma família. Peça ao dono para adicionar o seu SteamID.");
        }
        if (memberRepository.existsByUserId(userId)) {
            throw new FamiliaJaExisteException("Você já está em uma família.");
        }
        if (!donoSteamIdConfigurado.isBlank() && !donoSteamIdConfigurado.equals(user.getSteamId())) {
            throw new AcessoNegadoException("Só a conta Steam configurada como dona pode criar a família.");
        }

        Family family = familyRepository.save(new Family(request.name().trim(), user));
        memberRepository.save(new FamilyMember(family, user, FamilyRole.DONO));
        return montar(family);
    }

    @Transactional
    public void sair(int userId) {
        FamilyMember membro = membro(userId);
        if (membro.ehDono()) {
            throw new AcessoNegadoException(
                    "O dono não pode sair da família. Exclua a família ou remova os membros antes.");
        }
        memberRepository.delete(membro);
    }

    @Transactional
    public void excluir(int userId) {
        FamilyMember membro = membro(userId);
        if (!membro.ehDono()) {
            throw new AcessoNegadoException("Só o dono pode excluir a família.");
        }
        Family family = membro.getFamily();
        inviteRepository.deleteByFamilyId(family.getId());
        memberRepository.deleteByFamilyId(family.getId());
        memberRepository.flush(); // garante que os membros saiam antes da família (FK)
        familyRepository.delete(family);
    }

    // ---------- membros ----------

    @Transactional
    public void removerMembro(int userId, int membroUserId) {
        FamilyMember dono = membro(userId);
        if (!dono.ehDono()) {
            throw new AcessoNegadoException("Só o dono da família pode remover membros.");
        }
        if (userId == membroUserId) {
            throw new AcessoNegadoException("O dono não pode remover a si mesmo da família.");
        }
        FamilyMember alvo = memberRepository.findByUserId(membroUserId)
                .orElseThrow(() -> new MembroNaoEncontradoException("Essa pessoa não está na família."));
        if (alvo.getFamily().getId() != dono.getFamily().getId()) {
            throw new AcessoNegadoException("Essa pessoa não é da sua família.");
        }
        memberRepository.delete(alvo);
    }

    // ---------- convites ----------

    @Transactional
    public FamilyInviteResponse convidar(int userId, InviteMemberRequest request) {
        FamilyMember autor = membro(userId);
        if (!autor.ehDono()) {
            throw new AcessoNegadoException("Só o dono da família pode adicionar pessoas pelo SteamID.");
        }

        Family family = autor.getFamily();
        String steamId = request.steamId().trim();

        if (steamId.equals(autor.getUser().getSteamId())) {
            throw new ConviteInvalidoException("Você não pode convidar o seu próprio SteamID.");
        }

        Optional<User> alvo = userRepository.findBySteamId(steamId);
        if (alvo.isPresent() && memberRepository.existsByUserId(alvo.get().getId())) {
            throw new ConviteInvalidoException(alvo.get().getDisplayName() + " já está na família.");
        }
        if (inviteRepository.existsByFamilyIdAndSteamIdAndStatus(family.getId(), steamId, InviteStatus.PENDENTE)) {
            throw new ConviteInvalidoException("Já existe um convite pendente para este SteamID.");
        }

        // Se a conta já existe no banco, o nome/avatar vêm dela; senão o convite fica só com o ID
        // e a pessoa aparece com o nome real quando entrar e aceitar.
        String nome = alvo.map(User::getDisplayName).orElse(null);
        String avatar = alvo.map(User::getAvatarUrl).orElse(null);

        FamilyInvite convite = inviteRepository.save(
                new FamilyInvite(family, steamId, nome, avatar, autor.getUser()));
        return FamilyInviteResponse.de(convite);
    }

    @Transactional(readOnly = true)
    public List<FamilyInviteResponse> convitesEnviados(int userId) {
        FamilyMember membro = membro(userId);
        if (!membro.ehDono()) {
            throw new AcessoNegadoException("Só o dono vê os convites enviados.");
        }
        return inviteRepository
                .findByFamilyIdAndStatusOrderByCreatedAtDesc(membro.getFamily().getId(), InviteStatus.PENDENTE)
                .stream()
                .map(FamilyInviteResponse::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FamilyInviteResponse> convitesRecebidos(int userId) {
        User user = usuario(userId);
        return inviteRepository
                .findBySteamIdAndStatusOrderByCreatedAtDesc(user.getSteamId(), InviteStatus.PENDENTE)
                .stream()
                .map(FamilyInviteResponse::de)
                .toList();
    }

    @Transactional
    public FamilyResponse aceitar(int userId, int conviteId) {
        User user = usuario(userId);
        FamilyInvite convite = convite(conviteId);

        if (!convite.getSteamId().equals(user.getSteamId())) {
            throw new AcessoNegadoException("Este convite foi feito para outro SteamID.");
        }
        if (memberRepository.existsByUserId(userId)) {
            throw new FamiliaJaExisteException("Você já está em uma família.");
        }

        memberRepository.save(new FamilyMember(convite.getFamily(), user, FamilyRole.MEMBRO));
        convite.aceitar();

        // Outros convites pendentes para a mesma pessoa deixam de fazer sentido.
        inviteRepository.findBySteamIdAndStatusOrderByCreatedAtDesc(user.getSteamId(), InviteStatus.PENDENTE)
                .forEach(outro -> outro.cancelar());

        return montar(convite.getFamily());
    }

    @Transactional
    public void recusar(int userId, int conviteId) {
        User user = usuario(userId);
        FamilyInvite convite = convite(conviteId);
        if (!convite.getSteamId().equals(user.getSteamId())) {
            throw new AcessoNegadoException("Este convite foi feito para outro SteamID.");
        }
        convite.recusar();
    }

    @Transactional
    public void cancelarConvite(int userId, int conviteId) {
        FamilyMember dono = membro(userId);
        FamilyInvite convite = convite(conviteId);
        if (!dono.ehDono() || convite.getFamily().getId() != dono.getFamily().getId()) {
            throw new AcessoNegadoException("Só o dono pode cancelar os convites da família.");
        }
        convite.cancelar();
    }

    // ---------- apoio ----------

    private FamilyResponse montar(Family family) {
        List<FamilyMember> membros = new ArrayList<>(memberRepository.findByFamilyId(family.getId()));
        membros.sort(Comparator
                .comparing((FamilyMember membro) -> !membro.ehDono()) // dono primeiro
                .thenComparing(FamilyMember::getJoinedAt));
        return FamilyResponse.de(family, membros);
    }

    private User usuario(int userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserIdNaoEncontradoNoBancoException("Usuário não encontrado: " + userId));
    }

    private FamilyMember membro(int userId) {
        return memberRepository.findByUserId(userId)
                .orElseThrow(() -> new AcessoNegadoException(
                        "Você ainda não faz parte de uma família. Peça ao dono para adicionar o seu SteamID."));
    }

    private FamilyInvite convite(int conviteId) {
        return inviteRepository.findById(conviteId)
                .orElseThrow(() -> new ConviteNaoEncontradoException("Convite não encontrado: " + conviteId));
    }
}
