package com.bembosevents.service;

import com.bembosevents.domain.TipoEventoSeguridad;
import com.bembosevents.entity.Usuario;
import com.bembosevents.entity.UsuarioSeguridad;
import com.bembosevents.repository.UsuarioRepository;
import com.bembosevents.repository.UsuarioSeguridadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginSecurityServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioSeguridadRepository
            usuarioSeguridadRepository;

    @Mock
    private SecurityAuditService securityAuditService;

    private LoginSecurityService service;

    @BeforeEach
    void setUp() {

        service =
                new LoginSecurityService(
                        usuarioRepository,
                        usuarioSeguridadRepository,
                        securityAuditService,
                        3,
                        15);
    }

    @Test
    void debeInicializarPerfilDeSeguridad() {

        Usuario usuario =
                usuario();

        when(usuarioSeguridadRepository
                .existsById(1))
                .thenReturn(false);

        service.initializeForCurrentRegistration(
                usuario);

        ArgumentCaptor<UsuarioSeguridad> captor =
                ArgumentCaptor.forClass(
                        UsuarioSeguridad.class);

        verify(usuarioSeguridadRepository)
                .save(captor.capture());

        UsuarioSeguridad estado =
                captor.getValue();

        assertEquals(
                usuario,
                estado.getUsuario());

        assertEquals(
                true,
                estado.getCorreoVerificado());

        assertEquals(
                0,
                estado.getIntentosFallidos());

        assertEquals(
                false,
                estado.getMfaHabilitado());

        assertNull(
                estado.getBloqueadoHasta());
    }

    @Test
    void debeIncrementarIntentoFallido() {

        Usuario usuario =
                usuario();

        UsuarioSeguridad estado =
                estado(
                        usuario,
                        0,
                        null);

        when(usuarioRepository
                .findByCorreoIgnoreCase(
                        "cliente@bembos.test"))
                .thenReturn(
                        Optional.of(usuario));

        when(usuarioSeguridadRepository
                .findByIdUsuarioForUpdate(1))
                .thenReturn(
                        Optional.of(estado));

        service.recordFailure(
                "cliente@bembos.test");

        assertEquals(
                1,
                estado.getIntentosFallidos());

        assertNull(
                estado.getBloqueadoHasta());

        verify(securityAuditService)
                .record(
                        usuario,
                        TipoEventoSeguridad.LOGIN_FALLIDO,
                        false,
                        "Credenciales invalidas");
    }

    @Test
    void debeBloquearAlAlcanzarMaximo() {

        Usuario usuario =
                usuario();

        UsuarioSeguridad estado =
                estado(
                        usuario,
                        2,
                        null);

        when(usuarioRepository
                .findByCorreoIgnoreCase(
                        "cliente@bembos.test"))
                .thenReturn(
                        Optional.of(usuario));

        when(usuarioSeguridadRepository
                .findByIdUsuarioForUpdate(1))
                .thenReturn(
                        Optional.of(estado));

        service.recordFailure(
                "cliente@bembos.test");

        assertEquals(
                3,
                estado.getIntentosFallidos());

        assertNotNull(
                estado.getBloqueadoHasta());

        assertTrue(
                estado.getBloqueadoHasta()
                        .isAfter(
                                LocalDateTime.now(
                                        ZoneOffset.UTC)));

        verify(securityAuditService)
                .record(
                        usuario,
                        TipoEventoSeguridad.USUARIO_BLOQUEADO,
                        true,
                        "Bloqueo temporal aplicado");
    }

    @Test
    void debeRechazarUsuarioBloqueado() {

        Usuario usuario =
                usuario();

        UsuarioSeguridad estado =
                estado(
                        usuario,
                        3,
                        LocalDateTime.now(
                                ZoneOffset.UTC)
                                .plusMinutes(5));

        when(usuarioRepository
                .findByCorreoIgnoreCase(
                        "cliente@bembos.test"))
                .thenReturn(
                        Optional.of(usuario));

        when(usuarioSeguridadRepository
                .findByIdUsuarioForUpdate(1))
                .thenReturn(
                        Optional.of(estado));

        BadCredentialsException ex =
                assertThrows(
                        BadCredentialsException.class,
                        () ->
                                service.validateLoginAllowed(
                                        "cliente@bembos.test"));

        assertEquals(
                "Credenciales invalidas",
                ex.getMessage());

        verify(usuarioSeguridadRepository, never())
                .save(any(UsuarioSeguridad.class));
    }

    @Test
    void debeDesbloquearCuandoExpira() {

        Usuario usuario =
                usuario();

        UsuarioSeguridad estado =
                estado(
                        usuario,
                        3,
                        LocalDateTime.now(
                                ZoneOffset.UTC)
                                .minusMinutes(1));

        when(usuarioRepository
                .findByCorreoIgnoreCase(
                        "cliente@bembos.test"))
                .thenReturn(
                        Optional.of(usuario));

        when(usuarioSeguridadRepository
                .findByIdUsuarioForUpdate(1))
                .thenReturn(
                        Optional.of(estado));

        service.validateLoginAllowed(
                "cliente@bembos.test");

        assertEquals(
                0,
                estado.getIntentosFallidos());

        assertNull(
                estado.getBloqueadoHasta());

        verify(securityAuditService)
                .record(
                        usuario,
                        TipoEventoSeguridad.USUARIO_DESBLOQUEADO,
                        true,
                        "Bloqueo temporal expirado");
    }

    @Test
    void debeReiniciarIntentosTrasLoginCorrecto() {

        Usuario usuario =
                usuario();

        UsuarioSeguridad estado =
                estado(
                        usuario,
                        2,
                        null);

        when(usuarioRepository
                .findByCorreoIgnoreCase(
                        "cliente@bembos.test"))
                .thenReturn(
                        Optional.of(usuario));

        when(usuarioSeguridadRepository
                .findByIdUsuarioForUpdate(1))
                .thenReturn(
                        Optional.of(estado));

        service.recordSuccess(
                "cliente@bembos.test");

        assertEquals(
                0,
                estado.getIntentosFallidos());

        assertNull(
                estado.getBloqueadoHasta());

        verify(securityAuditService)
                .record(
                        usuario,
                        TipoEventoSeguridad.LOGIN_EXITOSO,
                        true,
                        "Login correcto");
    }

    @Test
    void correoInexistenteNoCreaEstado() {

        when(usuarioRepository
                .findByCorreoIgnoreCase(
                        "nadie@bembos.test"))
                .thenReturn(
                        Optional.empty());

        service.recordFailure(
                "nadie@bembos.test");

        verify(usuarioSeguridadRepository, never())
                .save(any(UsuarioSeguridad.class));

        verify(securityAuditService)
                .record(
                        isNull(),
                        eq(TipoEventoSeguridad.LOGIN_FALLIDO),
                        eq(false),
                        anyString());
    }

    private Usuario usuario() {

        Usuario usuario =
                new Usuario();

        usuario.setIdUsuario(1);
        usuario.setCorreo(
                "cliente@bembos.test");

        return usuario;
    }

    private UsuarioSeguridad estado(
            Usuario usuario,
            int intentos,
            LocalDateTime bloqueadoHasta) {

        UsuarioSeguridad estado =
                new UsuarioSeguridad();

        estado.setUsuario(usuario);
        estado.setCorreoVerificado(true);
        estado.setIntentosFallidos(intentos);
        estado.setBloqueadoHasta(
                bloqueadoHasta);
        estado.setMfaHabilitado(false);

        return estado;
    }
}
