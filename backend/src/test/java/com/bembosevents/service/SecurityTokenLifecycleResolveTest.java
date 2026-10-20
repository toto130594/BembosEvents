package com.bembosevents.service;

import com.bembosevents.domain.TipoTokenSeguridad;
import com.bembosevents.entity.TokenSeguridadUsuario;
import com.bembosevents.entity.Usuario;
import com.bembosevents.repository.TokenSeguridadUsuarioRepository;
import com.bembosevents.security.SecurityTokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityTokenLifecycleResolveTest {

    @Mock
    private TokenSeguridadUsuarioRepository
            tokenSeguridadUsuarioRepository;

    @Mock
    private SecurityTokenService
            securityTokenService;

    @Test
    void resolveActivoNoConsumeToken() {

        SecurityTokenLifecycleService service =
                new SecurityTokenLifecycleService(
                        tokenSeguridadUsuarioRepository,
                        securityTokenService);

        Usuario usuario =
                new Usuario();

        usuario.setIdUsuario(1);
        usuario.setCorreo(
                "cliente@bembos.test");

        TokenSeguridadUsuario token =
                new TokenSeguridadUsuario();

        token.setUsuario(usuario);
        token.setTipo(
                TipoTokenSeguridad.MFA_LOGIN_CHALLENGE);
        token.setTokenHash(
                "hash-token");

        token.setFechaCreacion(
                LocalDateTime.now(
                        ZoneOffset.UTC)
                        .minusMinutes(1));

        token.setFechaExpiracion(
                LocalDateTime.now(
                        ZoneOffset.UTC)
                        .plusMinutes(5));

        token.setFechaUso(null);
        token.setFechaRevocacion(null);

        when(securityTokenService
                .hashToken(
                        "challenge-raw"))
                .thenReturn(
                        "hash-token");

        when(tokenSeguridadUsuarioRepository
                .findForUpdate(
                        "hash-token",
                        TipoTokenSeguridad.MFA_LOGIN_CHALLENGE))
                .thenReturn(
                        Optional.of(token));

        Usuario resolved =
                service.resolveActive(
                        "challenge-raw",
                        TipoTokenSeguridad.MFA_LOGIN_CHALLENGE);

        assertEquals(
                usuario,
                resolved);

        assertNull(
                token.getFechaUso());

        verify(tokenSeguridadUsuarioRepository, never())
                .save(token);
    }
}