package com.bembosevents.service;

import com.bembosevents.domain.TipoEventoSeguridad;
import com.bembosevents.entity.RefreshTokenHistory;
import com.bembosevents.entity.SesionUsuario;
import com.bembosevents.entity.Usuario;
import com.bembosevents.repository.RefreshTokenHistoryRepository;
import com.bembosevents.repository.SesionUsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenReuseServiceTest {

    @Mock
    private RefreshTokenHistoryRepository
            refreshTokenHistoryRepository;

    @Mock
    private SesionUsuarioRepository
            sesionUsuarioRepository;

    @Mock
    private SecurityAuditService
            securityAuditService;

    private RefreshTokenReuseService service;

    @BeforeEach
    void setUp() {

        service =
                new RefreshTokenReuseService(
                        refreshTokenHistoryRepository,
                        sesionUsuarioRepository,
                        securityAuditService);
    }

    @Test
    void debeDetectarReuseRevocarSesionYAuditar() {

        Usuario usuario =
                new Usuario();

        usuario.setIdUsuario(1);
        usuario.setCorreo(
                "cliente@bembos.test");

        SesionUsuario sesion =
                new SesionUsuario();

        sesion.setIdSesionUsuario(10L);
        sesion.setUsuario(usuario);

        sesion.setFechaExpiracion(
                LocalDateTime.now(
                        ZoneOffset.UTC)
                        .plusDays(5));

        sesion.setFechaRevocacion(null);

        RefreshTokenHistory history =
                new RefreshTokenHistory();

        history.setIdRefreshTokenHistory(100L);
        history.setSesionUsuario(
                sesion);

        history.setRefreshTokenHash(
                "hash-refresh-anterior");

        history.setFechaRotacion(
                LocalDateTime.now(
                        ZoneOffset.UTC)
                        .minusMinutes(5));

        history.setFechaExpiracion(
                sesion.getFechaExpiracion());

        when(refreshTokenHistoryRepository
                .findByRefreshTokenHashForUpdate(
                        "hash-refresh-anterior"))
                .thenReturn(
                        Optional.of(
                                history));

        when(sesionUsuarioRepository
                .findByIdSesionUsuarioForUpdate(
                        10L))
                .thenReturn(
                        Optional.of(
                                sesion));

        boolean reused =
                service.detectAndRevokeIfReused(
                        "hash-refresh-anterior");

        assertTrue(
                reused);

        assertNotNull(
                sesion.getFechaRevocacion());

        verify(sesionUsuarioRepository)
                .save(
                        sesion);

        verify(securityAuditService)
                .record(
                        usuario,
                        TipoEventoSeguridad.REFRESH_TOKEN_REUTILIZADO,
                        false,
                        "Reutilizacion de refresh token detectada; sesion revocada");
    }

    @Test
    void debeIgnorarHashQueNoEstaEnHistorial() {

        when(refreshTokenHistoryRepository
                .findByRefreshTokenHashForUpdate(
                        "hash-desconocido"))
                .thenReturn(
                        Optional.empty());

        boolean reused =
                service.detectAndRevokeIfReused(
                        "hash-desconocido");

        assertFalse(
                reused);

        verify(
                sesionUsuarioRepository,
                never())
                .findByIdSesionUsuarioForUpdate(
                        10L);

        verify(
                securityAuditService,
                never())
                .record(
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.anyBoolean(),
                        org.mockito.ArgumentMatchers.any());
    }

    @Test
    void debeDetectarReuseAunqueSesionYaEsteRevocada() {

        Usuario usuario =
                new Usuario();

        usuario.setIdUsuario(1);
        usuario.setCorreo(
                "cliente@bembos.test");

        SesionUsuario sesion =
                new SesionUsuario();

        sesion.setIdSesionUsuario(10L);
        sesion.setUsuario(usuario);

        sesion.setFechaRevocacion(
                LocalDateTime.now(
                        ZoneOffset.UTC)
                        .minusMinutes(1));

        RefreshTokenHistory history =
                new RefreshTokenHistory();

        history.setSesionUsuario(
                sesion);

        history.setRefreshTokenHash(
                "hash-replay");

        when(refreshTokenHistoryRepository
                .findByRefreshTokenHashForUpdate(
                        "hash-replay"))
                .thenReturn(
                        Optional.of(
                                history));

        when(sesionUsuarioRepository
                .findByIdSesionUsuarioForUpdate(
                        10L))
                .thenReturn(
                        Optional.of(
                                sesion));

        boolean reused =
                service.detectAndRevokeIfReused(
                        "hash-replay");

        assertTrue(
                reused);

        verify(
                sesionUsuarioRepository,
                never())
                .save(
                        sesion);

        verify(securityAuditService)
                .record(
                        usuario,
                        TipoEventoSeguridad.REFRESH_TOKEN_REUTILIZADO,
                        false,
                        "Reutilizacion de refresh token detectada; sesion revocada");
    }
}