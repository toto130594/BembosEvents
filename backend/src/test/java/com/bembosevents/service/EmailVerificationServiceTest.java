package com.bembosevents.service;

import com.bembosevents.domain.TipoEventoSeguridad;
import com.bembosevents.domain.TipoTokenSeguridad;
import com.bembosevents.entity.Usuario;
import com.bembosevents.entity.UsuarioSeguridad;
import com.bembosevents.repository.UsuarioRepository;
import com.bembosevents.repository.UsuarioSeguridadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

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
class EmailVerificationServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioSeguridadRepository
            usuarioSeguridadRepository;

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

    private EmailVerificationService service;

    @BeforeEach
    void setUp() {

        service =
                new EmailVerificationService(
                        usuarioRepository,
                        usuarioSeguridadRepository,
                        securityTokenLifecycleService,
                        securityAuditService,
                        securityEmailServiceProvider,
                        24);
    }

    @Test
    void correoInexistenteNoGeneraToken() {

        when(usuarioRepository
                .findByCorreoIgnoreCase(
                        "nadie@bembos.test"))
                .thenReturn(
                        Optional.empty());

        service.solicitarReenvio(
                "NADIE@BEMBOS.TEST");

        verify(securityTokenLifecycleService, never())
                .issue(
                        any(),
                        any(),
                        any());

        verify(securityAuditService)
                .record(
                        isNull(),
                        eq(TipoEventoSeguridad.VERIFICACION_CORREO_REENVIADA),
                        eq(false),
                        any());
    }

    @Test
    void usuarioYaVerificadoNoGeneraToken() {

        Usuario usuario =
                usuario();

        UsuarioSeguridad seguridad =
                estado(
                        usuario,
                        true);

        when(usuarioRepository
                .findByCorreoIgnoreCase(
                        "cliente@bembos.test"))
                .thenReturn(
                        Optional.of(usuario));

        when(usuarioSeguridadRepository
                .findByIdUsuarioForUpdate(1))
                .thenReturn(
                        Optional.of(seguridad));

        service.solicitarReenvio(
                "cliente@bembos.test");

        verify(securityTokenLifecycleService, never())
                .issue(
                        any(),
                        any(),
                        any());

        verify(securityEmailService, never())
                .sendEmailVerification(
                        any(),
                        any());
    }

    @Test
    void sinProveedorCorreoNoGeneraToken() {

        Usuario usuario =
                usuario();

        UsuarioSeguridad seguridad =
                estado(
                        usuario,
                        false);

        when(usuarioRepository
                .findByCorreoIgnoreCase(
                        "cliente@bembos.test"))
                .thenReturn(
                        Optional.of(usuario));

        when(usuarioSeguridadRepository
                .findByIdUsuarioForUpdate(1))
                .thenReturn(
                        Optional.of(seguridad));

        when(securityEmailServiceProvider
                .getIfAvailable())
                .thenReturn(null);

        service.solicitarReenvio(
                "cliente@bembos.test");

        verify(securityTokenLifecycleService, never())
                .issue(
                        any(),
                        any(),
                        any());
    }

    @Test
    void debeEmitirTokenParaUsuarioNoVerificado() {

        Usuario usuario =
                usuario();

        UsuarioSeguridad seguridad =
                estado(
                        usuario,
                        false);

        when(usuarioRepository
                .findByCorreoIgnoreCase(
                        "cliente@bembos.test"))
                .thenReturn(
                        Optional.of(usuario));

        when(usuarioSeguridadRepository
                .findByIdUsuarioForUpdate(1))
                .thenReturn(
                        Optional.of(seguridad));

        when(securityEmailServiceProvider
                .getIfAvailable())
                .thenReturn(
                        securityEmailService);

        when(securityTokenLifecycleService
                .issue(
                        eq(usuario),
                        eq(TipoTokenSeguridad.VERIFICACION_CORREO),
                        any()))
                .thenReturn(
                        new SecurityTokenLifecycleService.IssuedToken(
                                "raw-token-secreto",
                                LocalDateTime.now()
                                        .plusHours(24)));

        service.solicitarReenvio(
                "cliente@bembos.test");

        verify(securityEmailService)
                .sendEmailVerification(
                        "cliente@bembos.test",
                        "raw-token-secreto");

        verify(securityAuditService)
                .record(
                        usuario,
                        TipoEventoSeguridad.VERIFICACION_CORREO_REENVIADA,
                        true,
                        "Token de verificacion emitido");
    }

    @Test
    void debeMarcarCorreoVerificado() {

        Usuario usuario =
                usuario();

        UsuarioSeguridad seguridad =
                estado(
                        usuario,
                        false);

        when(securityTokenLifecycleService
                .consume(
                        "raw-token",
                        TipoTokenSeguridad.VERIFICACION_CORREO))
                .thenReturn(
                        usuario);

        when(usuarioSeguridadRepository
                .findByIdUsuarioForUpdate(1))
                .thenReturn(
                        Optional.of(seguridad));

        service.verificar(
                "raw-token");

        assertEquals(
                true,
                seguridad.getCorreoVerificado());

        verify(usuarioSeguridadRepository)
                .save(seguridad);

        verify(securityTokenLifecycleService)
                .revokeActive(
                        usuario,
                        TipoTokenSeguridad.VERIFICACION_CORREO);

        verify(securityAuditService)
                .record(
                        usuario,
                        TipoEventoSeguridad.CORREO_VERIFICADO,
                        true,
                        "Correo verificado correctamente");
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

    private UsuarioSeguridad estado(
            Usuario usuario,
            boolean verificado) {

        UsuarioSeguridad seguridad =
                new UsuarioSeguridad();

        seguridad.setUsuario(usuario);
        seguridad.setCorreoVerificado(
                verificado);
        seguridad.setIntentosFallidos(0);
        seguridad.setBloqueadoHasta(null);
        seguridad.setMfaHabilitado(false);

        return seguridad;
    }
}
