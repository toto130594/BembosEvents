package com.bembosevents.service;

import com.bembosevents.domain.TipoTokenSeguridad;
import com.bembosevents.entity.TokenSeguridadUsuario;
import com.bembosevents.entity.Usuario;
import com.bembosevents.exception.TokenSeguridadInvalidoException;
import com.bembosevents.repository.TokenSeguridadUsuarioRepository;
import com.bembosevents.security.SecurityTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityTokenLifecycleServiceTest {

    @Mock
    private TokenSeguridadUsuarioRepository
            tokenSeguridadUsuarioRepository;

    @Mock
    private SecurityTokenService securityTokenService;

    private SecurityTokenLifecycleService service;

    @BeforeEach
    void setUp() {

        service =
                new SecurityTokenLifecycleService(
                        tokenSeguridadUsuarioRepository,
                        securityTokenService);
    }

    @Test
    void debeEmitirTokenYGuardarSoloHash() {

        Usuario usuario =
                usuario();

        when(securityTokenService
                .generateOpaqueToken())
                .thenReturn(
                        "raw-token-secreto");

        when(securityTokenService
                .hashToken(
                        "raw-token-secreto"))
                .thenReturn(
                        "hash-token");

        SecurityTokenLifecycleService.IssuedToken issued =
                service.issue(
                        usuario,
                        TipoTokenSeguridad.RECUPERACION_PASSWORD,
                        Duration.ofMinutes(30));

        assertEquals(
                "raw-token-secreto",
                issued.rawToken());

        assertNotNull(
                issued.expiresAt());

        ArgumentCaptor<TokenSeguridadUsuario> captor =
                ArgumentCaptor.forClass(
                        TokenSeguridadUsuario.class);

        verify(tokenSeguridadUsuarioRepository)
                .save(captor.capture());

        TokenSeguridadUsuario persisted =
                captor.getValue();

        assertEquals(
                "hash-token",
                persisted.getTokenHash());

        assertNotEquals(
                issued.rawToken(),
                persisted.getTokenHash());

        assertEquals(
                TipoTokenSeguridad.RECUPERACION_PASSWORD,
                persisted.getTipo());

        verify(tokenSeguridadUsuarioRepository)
                .revokeActiveTokens(
                        eq(1),
                        eq(TipoTokenSeguridad.RECUPERACION_PASSWORD),
                        any(LocalDateTime.class));
    }

    @Test
    void debeRevocarTokenAnteriorAntesDeEmitirOtro() {

        Usuario usuario =
                usuario();

        when(securityTokenService
                .generateOpaqueToken())
                .thenReturn(
                        "nuevo-token");

        when(securityTokenService
                .hashToken(
                        "nuevo-token"))
                .thenReturn(
                        "nuevo-hash");

        service.issue(
                usuario,
                TipoTokenSeguridad.VERIFICACION_CORREO,
                Duration.ofHours(24));

        verify(tokenSeguridadUsuarioRepository)
                .revokeActiveTokens(
                        eq(1),
                        eq(TipoTokenSeguridad.VERIFICACION_CORREO),
                        any(LocalDateTime.class));
    }

    @Test
    void debeConsumirTokenValidoUnaSolaVez() {

        Usuario usuario =
                usuario();

        TokenSeguridadUsuario token =
                token(
                        usuario);

        when(securityTokenService
                .hashToken(
                        "raw-token"))
                .thenReturn(
                        "hash-token");

        when(tokenSeguridadUsuarioRepository
                .findForUpdate(
                        "hash-token",
                        TipoTokenSeguridad.RECUPERACION_PASSWORD))
                .thenReturn(
                        Optional.of(token));

        Usuario consumed =
                service.consume(
                        "raw-token",
                        TipoTokenSeguridad.RECUPERACION_PASSWORD);

        assertEquals(
                usuario,
                consumed);

        assertNotNull(
                token.getFechaUso());

        verify(tokenSeguridadUsuarioRepository)
                .save(token);
    }

    @Test
    void debeRechazarTokenExpirado() {

        Usuario usuario =
                usuario();

        TokenSeguridadUsuario token =
                token(
                        usuario);

        token.setFechaExpiracion(
                LocalDateTime.now(ZoneOffset.UTC)
                        .minusMinutes(1));

        when(securityTokenService
                .hashToken(
                        "raw-token"))
                .thenReturn(
                        "hash-token");

        when(tokenSeguridadUsuarioRepository
                .findForUpdate(
                        "hash-token",
                        TipoTokenSeguridad.RECUPERACION_PASSWORD))
                .thenReturn(
                        Optional.of(token));

        assertThrows(
                TokenSeguridadInvalidoException.class,
                () ->
                        service.consume(
                                "raw-token",
                                TipoTokenSeguridad.RECUPERACION_PASSWORD));
    }

    @Test
    void debeRechazarTokenYaUsado() {

        Usuario usuario =
                usuario();

        TokenSeguridadUsuario token =
                token(
                        usuario);

        token.setFechaUso(
                LocalDateTime.now(ZoneOffset.UTC));

        when(securityTokenService
                .hashToken(
                        "raw-token"))
                .thenReturn(
                        "hash-token");

        when(tokenSeguridadUsuarioRepository
                .findForUpdate(
                        "hash-token",
                        TipoTokenSeguridad.VERIFICACION_CORREO))
                .thenReturn(
                        Optional.of(token));

        assertThrows(
                TokenSeguridadInvalidoException.class,
                () ->
                        service.consume(
                                "raw-token",
                                TipoTokenSeguridad.VERIFICACION_CORREO));
    }

    @Test
    void debeRechazarTokenRevocado() {

        Usuario usuario =
                usuario();

        TokenSeguridadUsuario token =
                token(
                        usuario);

        token.setFechaRevocacion(
                LocalDateTime.now(ZoneOffset.UTC));

        when(securityTokenService
                .hashToken(
                        "raw-token"))
                .thenReturn(
                        "hash-token");

        when(tokenSeguridadUsuarioRepository
                .findForUpdate(
                        "hash-token",
                        TipoTokenSeguridad.VERIFICACION_CORREO))
                .thenReturn(
                        Optional.of(token));

        assertThrows(
                TokenSeguridadInvalidoException.class,
                () ->
                        service.consume(
                                "raw-token",
                                TipoTokenSeguridad.VERIFICACION_CORREO));
    }

    @Test
    void debeRechazarTokenDesconocido() {

        when(securityTokenService
                .hashToken(
                        "raw-token"))
                .thenReturn(
                        "hash-token");

        when(tokenSeguridadUsuarioRepository
                .findForUpdate(
                        "hash-token",
                        TipoTokenSeguridad.RECUPERACION_PASSWORD))
                .thenReturn(
                        Optional.empty());

        assertThrows(
                TokenSeguridadInvalidoException.class,
                () ->
                        service.consume(
                                "raw-token",
                                TipoTokenSeguridad.RECUPERACION_PASSWORD));
    }

    @Test
    void ttlDebeSerPositivo() {

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.issue(
                                usuario(),
                                TipoTokenSeguridad.RECUPERACION_PASSWORD,
                                Duration.ZERO));
    }

    private Usuario usuario() {

        Usuario usuario =
                new Usuario();

        usuario.setIdUsuario(1);
        usuario.setCorreo(
                "cliente@bembos.test");

        return usuario;
    }

    private TokenSeguridadUsuario token(
            Usuario usuario) {

        TokenSeguridadUsuario token =
                new TokenSeguridadUsuario();

        token.setUsuario(usuario);
        token.setTipo(
                TipoTokenSeguridad.RECUPERACION_PASSWORD);
        token.setTokenHash(
                "hash-token");
        token.setFechaCreacion(
                LocalDateTime.now(ZoneOffset.UTC)
                        .minusMinutes(1));
        token.setFechaExpiracion(
                LocalDateTime.now(ZoneOffset.UTC)
                        .plusMinutes(30));
        token.setFechaUso(null);
        token.setFechaRevocacion(null);

        return token;
    }
}
