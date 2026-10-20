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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthSessionRefreshServiceTest {

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
    void debeRotarRefreshYEmitirNuevoAccessYRefresh() {

        Usuario usuario =
                usuario();

        LocalDateTime expiration =
                LocalDateTime.now(
                        ZoneOffset.UTC)
                        .plusDays(10);

        when(sesionUsuarioService
                .rotar(
                        "refresh-anterior"))
                .thenReturn(
                        new SesionUsuarioService
                                .IssuedRefreshToken(
                                        "refresh-nuevo",
                                        expiration,
                                        usuario));

        UserDetails userDetails =
                User.withUsername(
                                "cliente@bembos.test")
                        .password("irrelevante")
                        .authorities("ROLE_CLIENTE")
                        .build();

        when(usuarioDetailsService
                .loadUserByUsername(
                        "cliente@bembos.test"))
                .thenReturn(
                        userDetails);

        when(jwtService
                .generateToken(
                        userDetails))
                .thenReturn(
                        "access-nuevo");

        when(jwtService
                .getExpirationMs())
                .thenReturn(
                        3600000L);

        AuthResponse response =
                service.refresh(
                        "refresh-anterior");

        assertEquals(
                "access-nuevo",
                response.token());

        assertEquals(
                "Bearer",
                response.tipo());

        assertEquals(
                3600000L,
                response.expiresInMs());

        assertEquals(
                "refresh-nuevo",
                response.refreshToken());

        assertTrue(
                response.refreshTokenExpiresInMs()
                        > 0);

        assertFalse(
                response.mfaRequired());

        assertNull(
                response.mfaChallengeToken());

        verify(sesionUsuarioService)
                .rotar(
                        "refresh-anterior");

        verify(sesionUsuarioService, never())
                .crear(
                        usuario,
                        null,
                        null);

        verify(jwtService)
                .generateToken(
                        userDetails);
    }

    @Test
    void debeRechazarResultadoRotadoSinUsuario() {

        LocalDateTime expiration =
                LocalDateTime.now(
                        ZoneOffset.UTC)
                        .plusDays(10);

        when(sesionUsuarioService
                .rotar(
                        "refresh-anterior"))
                .thenReturn(
                        new SesionUsuarioService
                                .IssuedRefreshToken(
                                        "refresh-nuevo",
                                        expiration));

        assertThrows(
                IllegalStateException.class,
                () -> service.refresh(
                        "refresh-anterior"));

        verify(jwtService, never())
                .generateToken(
                        org.mockito.ArgumentMatchers
                                .any(UserDetails.class));
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