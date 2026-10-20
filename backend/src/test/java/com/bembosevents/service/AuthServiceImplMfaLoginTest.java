package com.bembosevents.service;

import com.bembosevents.dto.auth.AuthResponse;
import com.bembosevents.dto.auth.LoginRequest;
import com.bembosevents.entity.Usuario;
import com.bembosevents.entity.UsuarioSeguridad;
import com.bembosevents.repository.ClienteRepository;
import com.bembosevents.repository.RolRepository;
import com.bembosevents.repository.UsuarioRepository;
import com.bembosevents.repository.UsuarioRolRepository;
import com.bembosevents.repository.UsuarioSeguridadRepository;
import com.bembosevents.security.JwtService;
import com.bembosevents.security.UsuarioDetailsService;
import com.bembosevents.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplMfaLoginTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RolRepository rolRepository;

    @Mock
    private UsuarioRolRepository usuarioRolRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UsuarioDetailsService usuarioDetailsService;

    @Mock
    private JwtService jwtService;

    @Mock
    private LoginSecurityService loginSecurityService;

    @Mock
    private UsuarioSeguridadRepository usuarioSeguridadRepository;

    @Mock
    private MfaLoginChallengeService mfaLoginChallengeService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void mfaHabilitadoDebeRetornarChallengeSinEmitirJwt() {

        LoginRequest request =
                new LoginRequest(
                        "CLIENTE@BEMBOS.TEST",
                        "Segura123");

        Authentication authentication =
                mock(Authentication.class);

        when(authenticationManager
                .authenticate(
                        any(Authentication.class)))
                .thenReturn(
                        authentication);

        Usuario usuario =
                new Usuario();

        usuario.setIdUsuario(
                1);

        usuario.setCorreo(
                "cliente@bembos.test");

        usuario.setActivo(
                true);

        UsuarioSeguridad seguridad =
                new UsuarioSeguridad();

        seguridad.setUsuario(
                usuario);

        seguridad.setMfaHabilitado(
                true);

        when(usuarioSeguridadRepository
                .findByUsuario_CorreoIgnoreCase(
                        "cliente@bembos.test"))
                .thenReturn(
                        Optional.of(seguridad));

        when(usuarioRepository
                .findByCorreoIgnoreCase(
                        "cliente@bembos.test"))
                .thenReturn(
                        Optional.of(usuario));

        LocalDateTime expiresAt =
                LocalDateTime.of(
                        2026,
                        10,
                        19,
                        18,
                        30);

        when(mfaLoginChallengeService
                .issue(
                        usuario))
                .thenReturn(
                        new MfaLoginChallengeService.IssuedChallenge(
                                "challenge-raw",
                                expiresAt));

        when(mfaLoginChallengeService
                .getChallengeTtlMs())
                .thenReturn(
                        300000L);

        AuthResponse response =
                authService.login(
                        request);

        assertTrue(
                response.mfaRequired());

        assertNull(
                response.token());

        assertNull(
                response.tipo());

        assertEquals(
                0L,
                response.expiresInMs());

        assertEquals(
                "challenge-raw",
                response.mfaChallengeToken());

        assertEquals(
                300000L,
                response.mfaChallengeExpiresInMs());

        verify(mfaLoginChallengeService)
                .issue(
                        usuario);

        verify(jwtService, never())
                .generateToken(
                        any(UserDetails.class));

        verify(usuarioDetailsService, never())
                .loadUserByUsername(
                        any(String.class));

        verify(loginSecurityService, never())
                .recordSuccess(
                        any(String.class));
    }
}