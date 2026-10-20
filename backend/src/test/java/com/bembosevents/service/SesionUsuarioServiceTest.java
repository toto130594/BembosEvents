package com.bembosevents.service;

import com.bembosevents.dto.auth.SesionResponse;

import com.bembosevents.domain.TipoEventoSeguridad;
import com.bembosevents.entity.SesionUsuario;
import com.bembosevents.entity.RefreshTokenHistory;
import com.bembosevents.exception.RecursoNoEncontradoException;
import com.bembosevents.entity.Usuario;
import com.bembosevents.repository.SesionUsuarioRepository;
import com.bembosevents.repository.RefreshTokenHistoryRepository;
import com.bembosevents.security.SecurityTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SesionUsuarioServiceTest {

    @Mock
    private SesionUsuarioRepository
            sesionUsuarioRepository;

    @Mock
    private SecurityTokenService
            securityTokenService;

    @Mock
    private SecurityAuditService
            securityAuditService;

    @Mock
    private RefreshTokenHistoryRepository
            refreshTokenHistoryRepository;

    @Mock
    private RefreshTokenReuseService
            refreshTokenReuseService;

    private SesionUsuarioService service;

    @BeforeEach
    void setUp() {

        service =
                new SesionUsuarioService(
                        sesionUsuarioRepository,
                        securityTokenService,
                        securityAuditService,
                        refreshTokenHistoryRepository,
                        refreshTokenReuseService,
                        30);
    }

    @Test
    void debeCrearSesionGuardandoSoloHash() {

        Usuario usuario =
                usuario();

        when(securityTokenService
                .generateOpaqueToken())
                .thenReturn(
                        "refresh-raw-secreto");

        when(securityTokenService
                .hashToken(
                        "refresh-raw-secreto"))
                .thenReturn(
                        "hash-refresh");

        SesionUsuarioService.IssuedRefreshToken issued =
                service.crear(
                        usuario,
                        "Browser Test",
                        "192.0.2.10");

        ArgumentCaptor<SesionUsuario> captor =
                ArgumentCaptor.forClass(
                        SesionUsuario.class);

        verify(sesionUsuarioRepository)
                .save(
                        captor.capture());

        SesionUsuario persisted =
                captor.getValue();

        assertEquals(
                "refresh-raw-secreto",
                issued.rawRefreshToken());

        assertEquals(
                "hash-refresh",
                persisted.getRefreshTokenHash());

        assertNotEquals(
                issued.rawRefreshToken(),
                persisted.getRefreshTokenHash());

        assertEquals(
                usuario,
                persisted.getUsuario());

        assertEquals(
                "Browser Test",
                persisted.getUserAgent());

        assertEquals(
                "192.0.2.10",
                persisted.getIpOrigen());

        assertNotNull(
                persisted.getFechaCreacion());

        assertNotNull(
                persisted.getFechaExpiracion());

        assertEquals(
                30,
                Duration.between(
                        persisted.getFechaCreacion(),
                        persisted.getFechaExpiracion())
                        .toDays());

        verify(securityAuditService)
                .record(
                        usuario,
                        TipoEventoSeguridad.SESION_CREADA,
                        true,
                        "Sesion persistente creada");
    }

    @Test
    void debeRotarTokenSinExtenderExpiracion() {

        Usuario usuario =
                usuario();

        LocalDateTime expiration =
                LocalDateTime.now(
                        ZoneOffset.UTC)
                        .plusDays(10);

        SesionUsuario sesion =
                sesion(
                        usuario,
                        expiration);

        when(securityTokenService
                .hashToken(
                        "refresh-anterior"))
                .thenReturn(
                        "hash-anterior");

        when(sesionUsuarioRepository
                .findByRefreshTokenHashForUpdate(
                        "hash-anterior"))
                .thenReturn(
                        Optional.of(
                                sesion));

        when(securityTokenService
                .matches(
                        "refresh-anterior",
                        "hash-anterior"))
                .thenReturn(
                        true);

        when(securityTokenService
                .generateOpaqueToken())
                .thenReturn(
                        "refresh-nuevo");

        when(securityTokenService
                .hashToken(
                        "refresh-nuevo"))
                .thenReturn(
                        "hash-nuevo");

        SesionUsuarioService.IssuedRefreshToken issued =
                service.rotar(
                        "refresh-anterior");

        assertEquals(
                "refresh-nuevo",
                issued.rawRefreshToken());

        assertEquals(
                expiration,
                issued.expiresAt());

        assertEquals(
                "hash-nuevo",
                sesion.getRefreshTokenHash());

        assertEquals(
                expiration,
                sesion.getFechaExpiracion());

        assertNotNull(
                sesion.getFechaUltimoUso());

        verify(sesionUsuarioRepository)
                .save(
                        sesion);

        verify(securityAuditService)
                .record(
                        usuario,
                        TipoEventoSeguridad.SESION_RENOVADA,
                        true,
                        "Sesion persistente renovada");
    }

    @Test
    void debeRechazarRefreshRevocado() {

        Usuario usuario =
                usuario();

        SesionUsuario sesion =
                sesion(
                        usuario,
                        LocalDateTime.now(
                                ZoneOffset.UTC)
                                .plusDays(5));

        sesion.setFechaRevocacion(
                LocalDateTime.now(
                        ZoneOffset.UTC));

        when(securityTokenService
                .hashToken(
                        "refresh"))
                .thenReturn(
                        "hash-refresh");

        when(sesionUsuarioRepository
                .findByRefreshTokenHashForUpdate(
                        "hash-refresh"))
                .thenReturn(
                        Optional.of(
                                sesion));

        assertThrows(
                BadCredentialsException.class,
                () -> service.rotar(
                        "refresh"));

        verify(securityTokenService, never())
                .generateOpaqueToken();

        verify(sesionUsuarioRepository, never())
                .save(
                        any(SesionUsuario.class));
    }

    @Test
    void debeRechazarRefreshExpirado() {

        Usuario usuario =
                usuario();

        SesionUsuario sesion =
                sesion(
                        usuario,
                        LocalDateTime.now(
                                ZoneOffset.UTC)
                                .minusSeconds(1));

        when(securityTokenService
                .hashToken(
                        "refresh"))
                .thenReturn(
                        "hash-refresh");

        when(sesionUsuarioRepository
                .findByRefreshTokenHashForUpdate(
                        "hash-refresh"))
                .thenReturn(
                        Optional.of(
                                sesion));

        assertThrows(
                BadCredentialsException.class,
                () -> service.rotar(
                        "refresh"));

        verify(securityTokenService, never())
                .generateOpaqueToken();
    }

    @Test
    void debeRechazarRefreshInexistente() {

        when(securityTokenService
                .hashToken(
                        "refresh"))
                .thenReturn(
                        "hash-refresh");

        when(sesionUsuarioRepository
                .findByRefreshTokenHashForUpdate(
                        "hash-refresh"))
                .thenReturn(
                        Optional.empty());

        assertThrows(
                BadCredentialsException.class,
                () -> service.rotar(
                        "refresh"));

        verify(securityTokenService, never())
                .generateOpaqueToken();
    }

    @Test
    void debeGuardarHashAnteriorEnHistorialAlRotar() {

        Usuario usuario =
                usuario();

        LocalDateTime expiration =
                LocalDateTime.now(
                        ZoneOffset.UTC)
                        .plusDays(10);

        SesionUsuario sesion =
                sesion(
                        usuario,
                        expiration);

        when(securityTokenService
                .hashToken(
                        "refresh-anterior-history"))
                .thenReturn(
                        "hash-anterior-history");

        when(sesionUsuarioRepository
                .findByRefreshTokenHashForUpdate(
                        "hash-anterior-history"))
                .thenReturn(
                        Optional.of(
                                sesion));

        when(securityTokenService
                .matches(
                        "refresh-anterior-history",
                        sesion.getRefreshTokenHash()))
                .thenReturn(
                        true);

        when(securityTokenService
                .generateOpaqueToken())
                .thenReturn(
                        "refresh-nuevo-history");

        when(securityTokenService
                .hashToken(
                        "refresh-nuevo-history"))
                .thenReturn(
                        "hash-nuevo-history");

        service.rotar(
                "refresh-anterior-history");

        ArgumentCaptor<RefreshTokenHistory>
                historyCaptor =
                ArgumentCaptor.forClass(
                        RefreshTokenHistory.class);

        verify(refreshTokenHistoryRepository)
                .save(
                        historyCaptor.capture());

        RefreshTokenHistory history =
                historyCaptor.getValue();

        assertEquals(
                sesion,
                history.getSesionUsuario());

        assertEquals(
                "hash-anterior-history",
                history.getRefreshTokenHash());

        assertNotNull(
                history.getFechaRotacion());

        assertEquals(
                expiration,
                history.getFechaExpiracion());

        assertEquals(
                "hash-nuevo-history",
                sesion.getRefreshTokenHash());
    }

    @Test
    void debeDetectarReuseDeRefreshAnterior() {

        when(securityTokenService
                .hashToken(
                        "refresh-reutilizado"))
                .thenReturn(
                        "hash-reutilizado");

        when(sesionUsuarioRepository
                .findByRefreshTokenHashForUpdate(
                        "hash-reutilizado"))
                .thenReturn(
                        Optional.empty());

        when(refreshTokenReuseService
                .detectAndRevokeIfReused(
                        "hash-reutilizado"))
                .thenReturn(
                        true);

        assertThrows(
                BadCredentialsException.class,
                () ->
                        service.rotar(
                                "refresh-reutilizado"));

        verify(refreshTokenReuseService)
                .detectAndRevokeIfReused(
                        "hash-reutilizado");

        verify(
                securityTokenService,
                never())
                .generateOpaqueToken();

        verify(
                refreshTokenHistoryRepository,
                never())
                .save(
                        any(RefreshTokenHistory.class));
    }
    private Usuario usuario() {

        Usuario usuario =
                new Usuario();

        usuario.setIdUsuario(1);
        usuario.setCorreo(
                "cliente@bembos.test");
        usuario.setActivo(true);

        return usuario;
    }

    private SesionUsuario sesion(
            Usuario usuario,
            LocalDateTime expiration) {

        SesionUsuario sesion =
                new SesionUsuario();

        sesion.setIdSesionUsuario(10L);
        sesion.setUsuario(usuario);
        sesion.setRefreshTokenHash(
                "hash-anterior");

        sesion.setFechaCreacion(
                LocalDateTime.now(
                        ZoneOffset.UTC)
                        .minusDays(1));

        sesion.setFechaUltimoUso(
                LocalDateTime.now(
                        ZoneOffset.UTC)
                        .minusHours(1));

        sesion.setFechaExpiracion(
                expiration);

        sesion.setFechaRevocacion(
                null);

        return sesion;
    }

    @Test
    void debeRevocarSesionActualPorRefreshToken() {

        Usuario usuario =
                usuario();

        LocalDateTime expiration =
                LocalDateTime.now(
                        ZoneOffset.UTC)
                        .plusDays(10);

        SesionUsuario sesion =
                sesion(
                        usuario,
                        expiration);

        sesion.setRefreshTokenHash(
                "hash-activo");

        when(securityTokenService
                .hashToken(
                        "refresh-activo"))
                .thenReturn(
                        "hash-activo");

        when(sesionUsuarioRepository
                .findByRefreshTokenHashForUpdate(
                        "hash-activo"))
                .thenReturn(
                        Optional.of(
                                sesion));

        when(securityTokenService
                .matches(
                        "refresh-activo",
                        "hash-activo"))
                .thenReturn(
                        true);

        service.revocar(
                "refresh-activo");

        assertNotNull(
                sesion.getFechaRevocacion());

        verify(sesionUsuarioRepository)
                .save(
                        sesion);

        verify(securityAuditService)
                .record(
                        usuario,
                        TipoEventoSeguridad.SESION_REVOCADA,
                        true,
                        "Sesion persistente revocada");
    }

    @Test
    void debeRechazarRevocacionDeRefreshYaRevocado() {

        Usuario usuario =
                usuario();

        LocalDateTime expiration =
                LocalDateTime.now(
                        ZoneOffset.UTC)
                        .plusDays(10);

        SesionUsuario sesion =
                sesion(
                        usuario,
                        expiration);

        sesion.setRefreshTokenHash(
                "hash-revocado");

        sesion.setFechaRevocacion(
                LocalDateTime.now(
                        ZoneOffset.UTC)
                        .minusMinutes(1));

        when(securityTokenService
                .hashToken(
                        "refresh-revocado"))
                .thenReturn(
                        "hash-revocado");

        when(sesionUsuarioRepository
                .findByRefreshTokenHashForUpdate(
                        "hash-revocado"))
                .thenReturn(
                        Optional.of(
                                sesion));

        assertThrows(
                BadCredentialsException.class,
                () -> service.revocar(
                        "refresh-revocado"));

        verify(
                sesionUsuarioRepository,
                never())
                .save(
                        sesion);
    }

    @Test
    void debeRevocarTodasLasSesionesDelUsuario() {

        Usuario usuario =
                usuario();

        when(sesionUsuarioRepository
                .revokeActiveSessions(
                        org.mockito.ArgumentMatchers.eq(
                                1),
                        org.mockito.ArgumentMatchers.any(
                                LocalDateTime.class)))
                .thenReturn(
                        2);

        int sesionesRevocadas =
                service.revocarTodas(
                        usuario);

        assertEquals(
                2,
                sesionesRevocadas);

        verify(sesionUsuarioRepository)
                .revokeActiveSessions(
                        org.mockito.ArgumentMatchers.eq(
                                1),
                        org.mockito.ArgumentMatchers.any(
                                LocalDateTime.class));

        verify(securityAuditService)
                .record(
                        usuario,
                        TipoEventoSeguridad.TODAS_SESIONES_REVOCADAS,
                        true,
                        "Sesiones persistentes revocadas: 2");
    }

    @Test
    void debeListarSolamenteSesionesActivasPropias() {

        Usuario usuario =
                usuario();

        LocalDateTime expiration =
                LocalDateTime.now(
                        ZoneOffset.UTC)
                        .plusDays(5);

        SesionUsuario sesion =
                sesion(
                        usuario,
                        expiration);

        sesion.setUserAgent(
                "Mozilla/5.0");

        sesion.setIpOrigen(
                "127.0.0.1");

        when(sesionUsuarioRepository
                .findAllByUsuario_IdUsuarioAndFechaRevocacionIsNullAndFechaExpiracionAfterOrderByFechaCreacionDesc(
                        org.mockito.ArgumentMatchers.eq(1),
                        org.mockito.ArgumentMatchers.any(
                                LocalDateTime.class)))
                .thenReturn(
                        List.of(
                                sesion));

        List<SesionResponse> sesiones =
                service.listarActivas(
                        usuario);

        assertEquals(
                1,
                sesiones.size());

        SesionResponse response =
                sesiones.get(0);

        assertEquals(
                10L,
                response.idSesionUsuario());

        assertEquals(
                "Mozilla/5.0",
                response.userAgent());

        assertEquals(
                "127.0.0.1",
                response.ipOrigen());
    }

    @Test
    void debeRevocarSesionPropiaEspecifica() {

        Usuario usuario =
                usuario();

        LocalDateTime expiration =
                LocalDateTime.now(
                        ZoneOffset.UTC)
                        .plusDays(5);

        SesionUsuario sesion =
                sesion(
                        usuario,
                        expiration);

        when(sesionUsuarioRepository
                .findActiveOwnedByIdForUpdate(
                        org.mockito.ArgumentMatchers.eq(10L),
                        org.mockito.ArgumentMatchers.eq(1),
                        org.mockito.ArgumentMatchers.any(
                                LocalDateTime.class)))
                .thenReturn(
                        Optional.of(
                                sesion));

        service.revocarPropia(
                usuario,
                10L);

        assertNotNull(
                sesion.getFechaRevocacion());

        verify(sesionUsuarioRepository)
                .save(
                        sesion);

        verify(securityAuditService)
                .record(
                        usuario,
                        TipoEventoSeguridad.SESION_REVOCADA,
                        true,
                        "Sesion persistente revocada");
    }

    @Test
    void debeOcultarSesionAjenaComoNoEncontrada() {

        Usuario usuario =
                usuario();

        when(sesionUsuarioRepository
                .findActiveOwnedByIdForUpdate(
                        org.mockito.ArgumentMatchers.eq(99L),
                        org.mockito.ArgumentMatchers.eq(1),
                        org.mockito.ArgumentMatchers.any(
                                LocalDateTime.class)))
                .thenReturn(
                        Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () ->
                        service.revocarPropia(
                                usuario,
                                99L));

        verify(
                sesionUsuarioRepository,
                never())
                .save(
                        any(SesionUsuario.class));
    }

    @Test
    void debeRechazarIdSesionNoPositivo() {

        Usuario usuario =
                usuario();

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.revocarPropia(
                                usuario,
                                0L));
    }
}
