package com.bembosevents.service;

import com.bembosevents.domain.TipoEventoSeguridad;
import com.bembosevents.domain.TipoTokenSeguridad;
import com.bembosevents.entity.Usuario;
import com.bembosevents.entity.UsuarioSeguridad;
import com.bembosevents.repository.SesionUsuarioRepository;
import com.bembosevents.repository.UsuarioRepository;
import com.bembosevents.repository.UsuarioSeguridadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordRecoveryServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioSeguridadRepository
            usuarioSeguridadRepository;

    @Mock
    private SesionUsuarioRepository
            sesionUsuarioRepository;

    @Mock
    private SecurityTokenLifecycleService
            securityTokenLifecycleService;

    @Mock
    private SecurityAuditService
            securityAuditService;

    @Mock
    private ObjectProvider<SecurityEmailService>
            securityEmailServiceProvider;

    @Mock
    private SecurityEmailService securityEmailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    private PasswordRecoveryService service;

    @BeforeEach
    void setUp() {

        service =
                new PasswordRecoveryService(
                        usuarioRepository,
                        usuarioSeguridadRepository,
                        sesionUsuarioRepository,
                        securityTokenLifecycleService,
                        securityAuditService,
                        securityEmailServiceProvider,
                        passwordEncoder,
                        30);
    }

    @Test
    void correoInexistenteNoGeneraToken() {

        when(usuarioRepository
                .findByCorreoIgnoreCase(
                        "nadie@bembos.test"))
                .thenReturn(
                        Optional.empty());

        service.solicitar(
                "NADIE@BEMBOS.TEST");

        verify(securityTokenLifecycleService, never())
                .issue(
                        any(),
                        any(),
                        any());

        verify(securityEmailService, never())
                .sendPasswordRecovery(
                        any(),
                        any());

        verify(securityAuditService)
                .record(
                        isNull(),
                        eq(TipoEventoSeguridad.PASSWORD_RESET_SOLICITADO),
                        eq(false),
                        any());
    }

    @Test
    void sinProveedorCorreoNoGeneraToken() {

        Usuario usuario =
                usuario();

        when(usuarioRepository
                .findByCorreoIgnoreCase(
                        "cliente@bembos.test"))
                .thenReturn(
                        Optional.of(usuario));

        when(securityEmailServiceProvider
                .getIfAvailable())
                .thenReturn(null);

        service.solicitar(
                "cliente@bembos.test");

        verify(securityTokenLifecycleService, never())
                .issue(
                        any(),
                        any(),
                        any());
    }

    @Test
    void debeEmitirYEnviarTokenSiExisteProveedor() {

        Usuario usuario =
                usuario();

        when(usuarioRepository
                .findByCorreoIgnoreCase(
                        "cliente@bembos.test"))
                .thenReturn(
                        Optional.of(usuario));

        when(securityEmailServiceProvider
                .getIfAvailable())
                .thenReturn(
                        securityEmailService);

        when(securityTokenLifecycleService
                .issue(
                        eq(usuario),
                        eq(TipoTokenSeguridad.RECUPERACION_PASSWORD),
                        any()))
                .thenReturn(
                        new SecurityTokenLifecycleService.IssuedToken(
                                "raw-token-secreto",
                                LocalDateTime.now()
                                        .plusMinutes(30)));

        service.solicitar(
                "cliente@bembos.test");

        verify(securityEmailService)
                .sendPasswordRecovery(
                        "cliente@bembos.test",
                        "raw-token-secreto");

        verify(securityAuditService)
                .record(
                        usuario,
                        TipoEventoSeguridad.PASSWORD_RESET_SOLICITADO,
                        true,
                        "Solicitud de recuperacion emitida");
    }

    @Test
    void debeRestablecerPasswordRevocarSesionesYLimpiarBloqueo() {

        Usuario usuario =
                usuario();

        UsuarioSeguridad seguridad =
                new UsuarioSeguridad();

        seguridad.setUsuario(usuario);
        seguridad.setCorreoVerificado(true);
        seguridad.setIntentosFallidos(3);
        seguridad.setBloqueadoHasta(
                LocalDateTime.now()
                        .plusMinutes(10));
        seguridad.setMfaHabilitado(false);

        when(securityTokenLifecycleService
                .consume(
                        "raw-token",
                        TipoTokenSeguridad.RECUPERACION_PASSWORD))
                .thenReturn(
                        usuario);

        when(passwordEncoder
                .encode(
                        "NuevaPassword123"))
                .thenReturn(
                        "$2a$hash-nuevo");

        when(usuarioSeguridadRepository
                .findByIdUsuarioForUpdate(1))
                .thenReturn(
                        Optional.of(seguridad));

        service.restablecer(
                "raw-token",
                "NuevaPassword123");

        assertEquals(
                "$2a$hash-nuevo",
                usuario.getPasswordHash());

        assertEquals(
                0,
                seguridad.getIntentosFallidos());

        assertEquals(
                null,
                seguridad.getBloqueadoHasta());

        verify(usuarioRepository)
                .save(usuario);

        verify(sesionUsuarioRepository)
                .revokeActiveSessions(
                        eq(1),
                        any(LocalDateTime.class));

        verify(securityTokenLifecycleService)
                .revokeActive(
                        usuario,
                        TipoTokenSeguridad.RECUPERACION_PASSWORD);

        verify(securityAuditService)
                .record(
                        usuario,
                        TipoEventoSeguridad.PASSWORD_RESET_COMPLETADO,
                        true,
                        "Password restablecido correctamente");
    }

    private Usuario usuario() {

        Usuario usuario =
                new Usuario();

        usuario.setIdUsuario(1);
        usuario.setCorreo(
                "cliente@bembos.test");
        usuario.setActivo(true);
        usuario.setPasswordHash(
                "$2a$hash-anterior");

        return usuario;
    }
}
