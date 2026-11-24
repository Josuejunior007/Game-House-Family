package io.github.josuejunior007.gamehousefamily.service;

import io.github.josuejunior007.gamehousefamily.dto.CreateFamilyRequest;
import io.github.josuejunior007.gamehousefamily.dto.FamilyInviteResponse;
import io.github.josuejunior007.gamehousefamily.dto.FamilyResponse;
import io.github.josuejunior007.gamehousefamily.dto.InviteMemberRequest;
import io.github.josuejunior007.gamehousefamily.exception.AcessoNegadoException;
import io.github.josuejunior007.gamehousefamily.exception.ConviteInvalidoException;
import io.github.josuejunior007.gamehousefamily.exception.FamiliaJaExisteException;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FamilyServiceTest {

    private static final String STEAM_DONO = "76561198000000001";
    private static final String STEAM_MEMBRO = "76561198000000002";
    private static final String STEAM_CONVIDADO = "76561198000000003";

    @Mock FamilyRepository familyRepository;
    @Mock FamilyMemberRepository memberRepository;
    @Mock FamilyInviteRepository inviteRepository;
    @Mock UserRepository userRepository;

    FamilyService service;
    User dono;
    User membro;
    Family family;

    @BeforeEach
    void setUp() {
        service = new FamilyService(familyRepository, memberRepository, inviteRepository, userRepository, "");
        dono = new User(STEAM_DONO, "Ana", null);
        ReflectionTestUtils.setField(dono, "id", 1);
        membro = new User(STEAM_MEMBRO, "Beto", null);
        ReflectionTestUtils.setField(membro, "id", 2);
        family = new Family("Casa", dono);
        ReflectionTestUtils.setField(family, "id", 10);
    }

    private FamilyMember vinculo(User user, FamilyRole papel, int id) {
        FamilyMember m = new FamilyMember(family, user, papel);
        ReflectionTestUtils.setField(m, "id", id);
        return m;
    }

    private FamilyMember donoVinculado() {
        FamilyMember m = vinculo(dono, FamilyRole.DONO, 1);
        when(memberRepository.findByUserId(1)).thenReturn(Optional.of(m));
        return m;
    }

    // ---------- criar ----------

    @Test
    void criarFazQuemCriouDono() {
        when(familyRepository.count()).thenReturn(0L);
        when(userRepository.findById(1)).thenReturn(Optional.of(dono));
        when(familyRepository.save(any(Family.class))).thenReturn(family);
        when(memberRepository.findByFamilyId(10)).thenReturn(List.of(vinculo(dono, FamilyRole.DONO, 1)));

        FamilyResponse resposta = service.criar(1, new CreateFamilyRequest("Casa"));

        assertThat(resposta.name()).isEqualTo("Casa");
        assertThat(resposta.ownerUserId()).isEqualTo(1);
        assertThat(resposta.members()).hasSize(1);
        assertThat(resposta.members().get(0).role()).isEqualTo(FamilyRole.DONO);
        verify(memberRepository).save(any(FamilyMember.class));
    }

    @Test
    void criarFalhaSeJaExisteFamilia() {
        when(familyRepository.count()).thenReturn(1L);
        when(userRepository.findById(1)).thenReturn(Optional.of(dono));

        assertThrows(FamiliaJaExisteException.class, () -> service.criar(1, new CreateFamilyRequest("Casa")));
        verify(familyRepository, never()).save(any());
    }

    @Test
    void criarFalhaSeSteamIdNaoEhODonoConfigurado() {
        FamilyService comDonoFixo = new FamilyService(familyRepository, memberRepository, inviteRepository,
                userRepository, "76561198000000099");
        when(familyRepository.count()).thenReturn(0L);
        when(userRepository.findById(1)).thenReturn(Optional.of(dono));

        assertThrows(AcessoNegadoException.class,
                () -> comDonoFixo.criar(1, new CreateFamilyRequest("Casa")));
    }

    // ---------- convidar ----------

    @Test
    void soODonoConvida() {
        when(memberRepository.findByUserId(2)).thenReturn(Optional.of(vinculo(membro, FamilyRole.MEMBRO, 2)));

        assertThrows(AcessoNegadoException.class,
                () -> service.convidar(2, new InviteMemberRequest(STEAM_CONVIDADO)));
        verify(inviteRepository, never()).save(any());
    }

    @Test
    void naoConvidaOProprioSteamId() {
        donoVinculado();

        assertThrows(ConviteInvalidoException.class,
                () -> service.convidar(1, new InviteMemberRequest(STEAM_DONO)));
    }

    @Test
    void naoConvidaQuemJaEhMembro() {
        donoVinculado();
        when(userRepository.findBySteamId(STEAM_MEMBRO)).thenReturn(Optional.of(membro));
        when(memberRepository.existsByUserId(2)).thenReturn(true);

        assertThrows(ConviteInvalidoException.class,
                () -> service.convidar(1, new InviteMemberRequest(STEAM_MEMBRO)));
    }

    @Test
    void naoDuplicaConvitePendente() {
        donoVinculado();
        when(userRepository.findBySteamId(STEAM_CONVIDADO)).thenReturn(Optional.empty());
        when(inviteRepository.existsByFamilyIdAndSteamIdAndStatus(10, STEAM_CONVIDADO, InviteStatus.PENDENTE))
                .thenReturn(true);

        assertThrows(ConviteInvalidoException.class,
                () -> service.convidar(1, new InviteMemberRequest(STEAM_CONVIDADO)));
    }

    @Test
    void conviteSalvoComoPendente() {
        donoVinculado();
        when(userRepository.findBySteamId(STEAM_CONVIDADO)).thenReturn(Optional.empty());
        when(inviteRepository.existsByFamilyIdAndSteamIdAndStatus(10, STEAM_CONVIDADO, InviteStatus.PENDENTE))
                .thenReturn(false);
        when(inviteRepository.save(any(FamilyInvite.class))).thenAnswer(inv -> inv.getArgument(0));

        FamilyInviteResponse resposta = service.convidar(1, new InviteMemberRequest(STEAM_CONVIDADO));

        assertThat(resposta.status()).isEqualTo(InviteStatus.PENDENTE);
        assertThat(resposta.steamId()).isEqualTo(STEAM_CONVIDADO);
        assertThat(resposta.familyName()).isEqualTo("Casa");
    }

    // ---------- aceitar ----------

    @Test
    void convidadoAceitaEViraMembro() {
        FamilyInvite convite = new FamilyInvite(family, STEAM_MEMBRO, "Beto", null, dono);
        ReflectionTestUtils.setField(convite, "id", 5);

        when(userRepository.findById(2)).thenReturn(Optional.of(membro));
        when(inviteRepository.findById(5)).thenReturn(Optional.of(convite));
        when(memberRepository.existsByUserId(2)).thenReturn(false);
        when(inviteRepository.findBySteamIdAndStatusOrderByCreatedAtDesc(STEAM_MEMBRO, InviteStatus.PENDENTE))
                .thenReturn(List.of());
        when(memberRepository.findByFamilyId(10)).thenReturn(List.of(vinculo(dono, FamilyRole.DONO, 1)));

        service.aceitar(2, 5);

        assertThat(convite.getStatus()).isEqualTo(InviteStatus.ACEITO);
        verify(memberRepository).save(any(FamilyMember.class));
    }

    @Test
    void naoAceitaConviteDeOutroSteamId() {
        FamilyInvite convite = new FamilyInvite(family, STEAM_CONVIDADO, null, null, dono);
        ReflectionTestUtils.setField(convite, "id", 6);

        when(userRepository.findById(2)).thenReturn(Optional.of(membro));
        when(inviteRepository.findById(6)).thenReturn(Optional.of(convite));

        assertThrows(AcessoNegadoException.class, () -> service.aceitar(2, 6));
        verify(memberRepository, never()).save(any());
    }

    // ---------- membros ----------

    @Test
    void donoNaoSaiDaPropriaFamilia() {
        donoVinculado();

        assertThrows(AcessoNegadoException.class, () -> service.sair(1));
        verify(memberRepository, never()).delete(any());
    }

    @Test
    void donoRemoveOutroMembro() {
        donoVinculado();
        FamilyMember alvo = vinculo(membro, FamilyRole.MEMBRO, 2);
        when(memberRepository.findByUserId(2)).thenReturn(Optional.of(alvo));

        service.removerMembro(1, 2);

        verify(memberRepository).delete(alvo);
    }

    @Test
    void donoNaoRemoveASiMesmo() {
        donoVinculado();

        assertThrows(AcessoNegadoException.class, () -> service.removerMembro(1, 1));
    }
}
