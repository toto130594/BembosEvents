package com.bembosevents.service;

import com.bembosevents.dto.auth.AuthResponse;
import com.bembosevents.entity.Usuario;
import com.bembosevents.security.JwtService;
import com.bembosevents.security.UsuarioDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthSessionIssuanceServiceTest {

    @Mock
    private UsuarioDetailsService
            usuarioDetailsService;

    @Mock
    private JwtService jwtService;

    @Mock
    private SesionUsuarioService
            sesionUsuarioService;

    private AuthSessionIssuanceService service;

    @BeforeEach
    void setUp() {

        service =
                new AuthSessionIssuanceService(
                        usuarioDetailsService,
                        jwtService,
                        sesionUsuarioService);
    }

    @Test
    void debeEmitirAccessJwtYRefreshToken() {

        Usuario usuario =
                usuario();

        UserDetails userDetails =
                User.withUsername(
                                "cliente@bembos.test")
                        .password("hash")
                        .roles("CLIENTE")
                        .build();

        LocalDateTime refreshExpiresAt =
                LocalDateTime.now(
                                ZoneOffset.UTC)
                        .plusDays(30);

        when(sesionUsuarioService
                .crear(
                        usuario,
                        null,
                        null))
                .thenReturn(
                        new SesionUsuarioService.IssuedRefreshToken(
                                "refresh-raw",
                                refreshExpiresAt));

        when(usuarioDetailsService
                .loadUserByUsername(
                        "cliente@bembos.test"))
                .thenReturn(
                        userDetails);

        when(jwtService
                .generateToken(
                        userDetails))
                .thenReturn(
                        "access-jwt");

        when(jwtService
                .getExpirationMs())
                .thenReturn(
                        3600000L);

        AuthResponse response =
                service.issue(
                        usuario);

        assertEquals(
                "access-jwt",
                response.token());

        assertEquals(
                "Bearer",
                response.tipo());

        assertEquals(
                3600000L,
                response.expiresInMs());

        assertEquals(
                "refresh-raw",
                response.refreshToken());

        assertTrue(
                response.refreshTokenExpiresInMs()
                        > 0);

        assertFalse(
                response.mfaRequired());

        assertNull(
                response.mfaChallengeToken());

        verify(sesionUsuarioService)
                .crear(
                        usuario,
                        null,
                        null);

        verify(jwtService)
                .generateToken(
                        userDetails);
    }

    @Test
    void usuarioNoPersistidoEsRechazado() {

        Usuario usuario =
                new Usuario();

        usuario.setCorreo(
                "cliente@bembos.test");

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.issue(
                                usuario));
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
}