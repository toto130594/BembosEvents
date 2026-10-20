package com.bembosevents.service;

import com.bembosevents.dto.auth.AuthResponse;
import com.bembosevents.dto.auth.LoginRequest;
import com.bembosevents.dto.auth.LogoutRequest;
import com.bembosevents.dto.auth.RefreshRequest;
import com.bembosevents.dto.auth.RegistroRequest;
import com.bembosevents.entity.Rol;
import com.bembosevents.entity.Usuario;
import com.bembosevents.entity.UsuarioSeguridad;
import com.bembosevents.entity.UsuarioRol;
import com.bembosevents.repository.RolRepository;
import com.bembosevents.repository.UsuarioRepository;
import com.bembosevents.repository.UsuarioSeguridadRepository;
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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {
    @Mock
    private AuthSessionIssuanceService
            authSessionIssuanceService;

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
    @Mock
    private SesionUsuarioService
            sesionUsuarioService;

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
    void debeRealizarLoginYRetornarAccessYRefresh() {

        LoginRequest request =
                new LoginRequest(
                        "CLIENTE@BEMBOS.TEST",
                        "Segura123");

        Usuario usuario =
                new Usuario();

        usuario.setIdUsuario(1);
        usuario.setCorreo(
                "cliente@bembos.test");
        usuario.setActivo(true);

        UsuarioSeguridad seguridad =
                new UsuarioSeguridad();

        seguridad.setUsuario(
                usuario);

        seguridad.setMfaHabilitado(
                false);

        when(usuarioSeguridadRepository
                .findByUsuario_CorreoIgnoreCase(
                        "cliente@bembos.test"))
                .thenReturn(
                        Optional.of(
                                seguridad));

        when(usuarioRepository
                .findByCorreoIgnoreCase(
                        "cliente@bembos.test"))
                .thenReturn(
                        Optional.of(
                                usuario));

        AuthResponse issued =
                AuthResponse.authenticated(
                        "access-jwt",
                        "Bearer",
                        3600000L,
                        "refresh-raw",
                        2592000000L);

        when(authSessionIssuanceService
                .issue(
                        usuario))
                .thenReturn(
                        issued);

        AuthResponse response =
                authService.login(
                        request);

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

        assertEquals(
                2592000000L,
                response.refreshTokenExpiresInMs());

        verify(authSessionIssuanceService)
                .issue(
                        usuario);

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

    @Test
    void debeDelegarRefreshASessionIssuanceService() {

        RefreshRequest request =
                new RefreshRequest(
                        "refresh-anterior");

        AuthResponse issued =
                AuthResponse.authenticated(
                        "access-nuevo",
                        "Bearer",
                        3600000L,
                        "refresh-nuevo",
                        864000000L);

        when(authSessionIssuanceService
                .refresh(
                        "refresh-anterior"))
                .thenReturn(
                        issued);

        AuthResponse response =
                authService.refresh(
                        request);

        assertEquals(
                "access-nuevo",
                response.token());

        assertEquals(
                "refresh-nuevo",
                response.refreshToken());

        verify(authSessionIssuanceService)
                .refresh(
                        "refresh-anterior");
    }

    @Test
    void debeDelegarLogoutActualASesionUsuarioService() {

        LogoutRequest request =
                new LogoutRequest(
                        "refresh-activo");

        authService.logout(
                request);

        verify(sesionUsuarioService)
                .revocar(
                        "refresh-activo");
    }

    @Test
    void debeRevocarTodasLasSesionesDelUsuarioAutenticado() {

        Usuario usuario =
                new Usuario();

        usuario.setIdUsuario(1);

        usuario.setCorreo(
                "cliente@bembos.test");

        usuario.setActivo(
                true);

        when(usuarioRepository
                .findByCorreoIgnoreCase(
                        "cliente@bembos.test"))
                .thenReturn(
                        Optional.of(
                                usuario));

        authService.logoutAll(
                "CLIENTE@BEMBOS.TEST");

        verify(usuarioRepository)
                .findByCorreoIgnoreCase(
                        "cliente@bembos.test");

        verify(sesionUsuarioService)
                .revocarTodas(
                        usuario);
    }

    @Test
    void debeRechazarLogoutAllSinPrincipal() {

        assertThrows(
                BadCredentialsException.class,
                () ->
                        authService.logoutAll(
                                null));

        verify(
                sesionUsuarioService,
                org.mockito.Mockito.never())
                .revocarTodas(
                        org.mockito.ArgumentMatchers
                                .any(Usuario.class));
    }

    @Test
    void debeListarSesionesDelUsuarioAutenticado() {

        Usuario usuario =
                new Usuario();

        usuario.setIdUsuario(1);
        usuario.setCorreo(
                "cliente@bembos.test");
        usuario.setActivo(
                true);

        when(usuarioRepository
                .findByCorreoIgnoreCase(
                        "cliente@bembos.test"))
                .thenReturn(
                        Optional.of(
                                usuario));

        when(sesionUsuarioService
                .listarActivas(
                        usuario))
                .thenReturn(
                        java.util.List.of());

        var sesiones =
                authService.listarSesiones(
                        "CLIENTE@BEMBOS.TEST");

        assertEquals(
                0,
                sesiones.size());

        verify(usuarioRepository)
                .findByCorreoIgnoreCase(
                        "cliente@bembos.test");

        verify(sesionUsuarioService)
                .listarActivas(
                        usuario);
    }

    @Test
    void debeRevocarSesionEspecificaDelUsuarioAutenticado() {

        Usuario usuario =
                new Usuario();

        usuario.setIdUsuario(1);
        usuario.setCorreo(
                "cliente@bembos.test");
        usuario.setActivo(
                true);

        when(usuarioRepository
                .findByCorreoIgnoreCase(
                        "cliente@bembos.test"))
                .thenReturn(
                        Optional.of(
                                usuario));

        authService.revocarSesion(
                "CLIENTE@BEMBOS.TEST",
                10L);

        verify(sesionUsuarioService)
                .revocarPropia(
                        usuario,
                        10L);
    }

    @Test
    void debeRechazarGestionSesionesSinUsuarioAutenticado() {

        assertThrows(
                BadCredentialsException.class,
                () ->
                        authService.listarSesiones(
                                null));

        assertThrows(
                BadCredentialsException.class,
                () ->
                        authService.revocarSesion(
                                null,
                                10L));

        verifyNoInteractions(
                sesionUsuarioService);
    }
}
