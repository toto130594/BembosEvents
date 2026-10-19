package com.bembosevents.service;

import com.bembosevents.dto.auth.AuthResponse;
import com.bembosevents.dto.auth.LoginRequest;
import com.bembosevents.dto.auth.RegistroRequest;
import com.bembosevents.entity.Rol;
import com.bembosevents.entity.Usuario;
import com.bembosevents.entity.UsuarioRol;
import com.bembosevents.repository.RolRepository;
import com.bembosevents.repository.UsuarioRepository;
import com.bembosevents.repository.UsuarioRolRepository;
import com.bembosevents.repository.ClienteRepository;
import com.bembosevents.security.JwtService;
import com.bembosevents.security.UsuarioDetailsService;
import com.bembosevents.service.LoginSecurityService;
import com.bembosevents.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

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

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void debeRegistrarClienteConPasswordHasheado() {

        RegistroRequest request =
                new RegistroRequest(
                        "Juan",
                        "Perez",
                        "JUAN@BEMBOS.TEST",
                        "999999999",
                        "Segura123");

        when(usuarioRepository
                .existsByCorreoIgnoreCase(
                        "juan@bembos.test"))
                .thenReturn(false);

        Rol rolCliente = new Rol();
        rolCliente.setIdRol(1);
        rolCliente.setNombre("CLIENTE");
        rolCliente.setActivo(true);

        when(rolRepository
                .findByNombreIgnoreCase("CLIENTE"))
                .thenReturn(Optional.of(rolCliente));

        when(passwordEncoder
                .encode("Segura123"))
                .thenReturn("$2a$10$hash");

        when(usuarioRepository
                .save(any(Usuario.class)))
                .thenAnswer(invocation -> {
                    Usuario usuario =
                            invocation.getArgument(0);
                    usuario.setIdUsuario(10);
                    return usuario;
                });

        authService.registrar(request);

        ArgumentCaptor<Usuario> usuarioCaptor =
                ArgumentCaptor.forClass(
                        Usuario.class);

        verify(usuarioRepository)
                .save(usuarioCaptor.capture());

        Usuario guardado =
                usuarioCaptor.getValue();

        assertEquals(
                "juan@bembos.test",
                guardado.getCorreo());

        assertEquals(
                "$2a$10$hash",
                guardado.getPasswordHash());

        assertEquals(
                true,
                guardado.getActivo());

        verify(usuarioRolRepository)
                .save(any(UsuarioRol.class));

        verify(loginSecurityService)
                .initializeForCurrentRegistration(
                        any(Usuario.class));

        verify(clienteRepository)
                .save(argThat(cliente ->
                        cliente.getUsuario() != null
                                && Integer.valueOf(10).equals(
                                        cliente.getUsuario().getIdUsuario())
                                && cliente.getDocumentoIdentidad() == null
                                && cliente.getFechaRegistro() != null
                                && Boolean.TRUE.equals(cliente.getActivo())));
}

    @Test
    void debeRechazarCorreoDuplicado() {

        RegistroRequest request =
                new RegistroRequest(
                        "Juan",
                        "Perez",
                        "juan@bembos.test",
                        null,
                        "Segura123");

        when(usuarioRepository
                .existsByCorreoIgnoreCase(
                        "juan@bembos.test"))
                .thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> authService.registrar(request));

        verify(usuarioRepository, never())
                .save(any(Usuario.class));
    }

    @Test
    void debeRealizarLoginYRetornarJwt() {

        LoginRequest request =
                new LoginRequest(
                        "CLIENTE@BEMBOS.TEST",
                        "Segura123");

        Authentication authentication =
                mock(Authentication.class);

        when(authenticationManager
                .authenticate(any(Authentication.class)))
                .thenReturn(authentication);

        UserDetails userDetails =
                User.withUsername(
                                "cliente@bembos.test")
                        .password("hash")
                        .roles("CLIENTE")
                        .build();

        when(usuarioDetailsService
                .loadUserByUsername(
                        "cliente@bembos.test"))
                .thenReturn(userDetails);

        when(jwtService
                .generateToken(userDetails))
                .thenReturn("jwt-token");

        when(jwtService
                .getExpirationMs())
                .thenReturn(3600000L);

        AuthResponse response =
                authService.login(request);

        assertEquals(
                "jwt-token",
                response.token());

        assertEquals(
                "Bearer",
                response.tipo());

        assertEquals(
                3600000L,
                response.expiresInMs());

        verify(loginSecurityService)
                .validateLoginAllowed(
                        "cliente@bembos.test");

        verify(loginSecurityService)
                .recordSuccess(
                        "cliente@bembos.test");
    }

    @Test
    void debeRechazarCredencialesInvalidas() {

        LoginRequest request =
                new LoginRequest(
                        "cliente@bembos.test",
                        "incorrecta");

        when(authenticationManager
                .authenticate(any(Authentication.class)))
                .thenThrow(
                        new BadCredentialsException(
                                "Bad credentials"));

        assertThrows(
                BadCredentialsException.class,
                () -> authService.login(request));

        verify(loginSecurityService)
                .validateLoginAllowed(
                        "cliente@bembos.test");

        verify(loginSecurityService)
                .recordFailure(
                        "cliente@bembos.test");
    }
    @Test
    void debeRechazarBloqueadoAntesDeAutenticar() {

        LoginRequest request =
                new LoginRequest(
                        "cliente@bembos.test",
                        "Segura123");

        org.mockito.Mockito
                .doThrow(
                        new BadCredentialsException(
                                "Credenciales invalidas"))
                .when(loginSecurityService)
                .validateLoginAllowed(
                        "cliente@bembos.test");

        BadCredentialsException ex =
                assertThrows(
                        BadCredentialsException.class,
                        () -> authService.login(request));

        assertEquals(
                "Credenciales invalidas",
                ex.getMessage());

        verify(authenticationManager, never())
                .authenticate(
                        any(Authentication.class));

        verify(loginSecurityService)
                .recordRejected(
                        "cliente@bembos.test");
    }
}
