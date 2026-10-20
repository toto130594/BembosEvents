package com.bembosevents.controller;

import com.bembosevents.dto.auth.AuthResponse;
import com.bembosevents.dto.auth.LoginRequest;
import com.bembosevents.dto.auth.LogoutRequest;
import com.bembosevents.dto.auth.RefreshRequest;
import com.bembosevents.dto.auth.RegistroRequest;
import com.bembosevents.exception.GlobalExceptionHandler;
import com.bembosevents.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {

        AuthController controller =
                new AuthController(authService);

        LocalValidatorFactoryBean validator =
                new LocalValidatorFactoryBean();

        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(
                        new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void debeRegistrarUsuario() throws Exception {

        String json = """
                {
                  "nombres": "Juan",
                  "apellidos": "Perez",
                  "correo": "juan@bembos.test",
                  "telefono": "999999999",
                  "password": "Segura123"
                }
                """;

        mockMvc.perform(
                        post("/api/auth/registro")
                                .contentType(
                                        MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(
                        status().isCreated())
                .andExpect(
                        jsonPath("$.mensaje")
                                .value(
                                        "Usuario registrado correctamente"));

        verify(authService)
                .registrar(
                        any(RegistroRequest.class));
    }

    @Test
    void debeRechazarRegistroInvalido()
            throws Exception {

        String json = """
                {
                  "nombres": "",
                  "apellidos": "Perez",
                  "correo": "correo-invalido",
                  "password": "123"
                }
                """;

        mockMvc.perform(
                        post("/api/auth/registro")
                                .contentType(
                                        MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(
                        status().isBadRequest())
                .andExpect(
                        jsonPath(
                                "$.validationErrors.correo")
                                .exists())
                .andExpect(
                        jsonPath(
                                "$.validationErrors.password")
                                .exists());

        verifyNoInteractions(
                authService);
    }

    @Test
    void debeRealizarLogin() throws Exception {

        when(authService.login(
                any(LoginRequest.class)))
                .thenReturn(
                        new AuthResponse(
                                "jwt-token",
                                "Bearer",
                                3600000L));

        String json = """
                {
                  "correo": "cliente@bembos.test",
                  "password": "Segura123"
                }
                """;

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(
                                        MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(
                        status().isOk())
                .andExpect(
                        jsonPath("$.token")
                                .value("jwt-token"))
                .andExpect(
                        jsonPath("$.tipo")
                                .value("Bearer"))
                .andExpect(
                        jsonPath("$.expiresInMs")
                                .value(3600000));
    }

    @Test
    void debeResponder401ConCredencialesInvalidas()
            throws Exception {

        when(authService.login(
                any(LoginRequest.class)))
                .thenThrow(
                        new BadCredentialsException(
                                "Bad credentials"));

        String json = """
                {
                  "correo": "cliente@bembos.test",
                  "password": "incorrecta"
                }
                """;

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(
                                        MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(
                        status().isUnauthorized())
                .andExpect(
                        jsonPath("$.error")
                                .value("Unauthorized"))
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Credenciales invalidas"));
    }

    @Test
    void debeRenovarSesion() throws Exception {

        when(authService.refresh(
                any(RefreshRequest.class)))
                .thenReturn(
                        AuthResponse.authenticated(
                                "access-nuevo",
                                "Bearer",
                                3600000L,
                                "refresh-nuevo",
                                864000000L));

        String json = """
                {
                  "refreshToken": "refresh-anterior"
                }
                """;

        mockMvc.perform(
                        post("/api/auth/refresh")
                                .contentType(
                                        MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(
                        status().isOk())
                .andExpect(
                        jsonPath("$.token")
                                .value("access-nuevo"))
                .andExpect(
                        jsonPath("$.tipo")
                                .value("Bearer"))
                .andExpect(
                        jsonPath("$.expiresInMs")
                                .value(3600000))
                .andExpect(
                        jsonPath("$.refreshToken")
                                .value("refresh-nuevo"))
                .andExpect(
                        jsonPath("$.refreshTokenExpiresInMs")
                                .value(864000000))
                .andExpect(
                        jsonPath("$.mfaRequired")
                                .value(false));

        verify(authService)
                .refresh(
                        any(RefreshRequest.class));
    }

    @Test
    void debeRechazarRefreshVacio()
            throws Exception {

        String json = """
                {
                  "refreshToken": ""
                }
                """;

        mockMvc.perform(
                        post("/api/auth/refresh")
                                .contentType(
                                        MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(
                        status().isBadRequest())
                .andExpect(
                        jsonPath(
                                "$.validationErrors.refreshToken")
                                .exists());

        verifyNoInteractions(
                authService);
    }

    @Test
    void debeResponder401ConRefreshInvalido()
            throws Exception {

        when(authService.refresh(
                any(RefreshRequest.class)))
                .thenThrow(
                        new BadCredentialsException(
                                "Refresh token invalido"));

        String json = """
                {
                  "refreshToken": "refresh-invalido"
                }
                """;

        mockMvc.perform(
                        post("/api/auth/refresh")
                                .contentType(
                                        MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(
                        status().isUnauthorized())
                .andExpect(
                        jsonPath("$.error")
                                .value("Unauthorized"))
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Credenciales invalidas"));
    }

    @Test
    void debeCerrarSesionActual()
            throws Exception {

        String json = """
                {
                  "refreshToken": "refresh-activo"
                }
                """;

        mockMvc.perform(
                        post("/api/auth/logout")
                                .contentType(
                                        MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(
                        status().isNoContent());

        verify(authService)
                .logout(
                        any(LogoutRequest.class));
    }

    @Test
    void debeRechazarLogoutSinRefresh()
            throws Exception {

        String json = """
                {
                  "refreshToken": ""
                }
                """;

        mockMvc.perform(
                        post("/api/auth/logout")
                                .contentType(
                                        MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(
                        status().isBadRequest())
                .andExpect(
                        jsonPath(
                                "$.validationErrors.refreshToken")
                                .exists());

        verify(
                authService,
                org.mockito.Mockito.never())
                .logout(
                        any(LogoutRequest.class));
    }

    @Test
    void debeResponder401ConRefreshLogoutInvalido()
            throws Exception {

        org.mockito.Mockito
                .doThrow(
                        new BadCredentialsException(
                                "Refresh token invalido"))
                .when(authService)
                .logout(
                        any(LogoutRequest.class));

        String json = """
                {
                  "refreshToken": "refresh-invalido"
                }
                """;

        mockMvc.perform(
                        post("/api/auth/logout")
                                .contentType(
                                        MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(
                        status().isUnauthorized())
                .andExpect(
                        jsonPath("$.error")
                                .value(
                                        "Unauthorized"));
    }

    @Test
    void debeCerrarTodasLasSesiones()
            throws Exception {

        mockMvc.perform(
                        post("/api/auth/logout-all")
                                .principal(
                                        () ->
                                                "cliente@bembos.test"))
                .andExpect(
                        status().isNoContent());

        verify(authService)
                .logoutAll(
                        "cliente@bembos.test");
    }

    @Test
    void debeListarSesionesPropias()
            throws Exception {

        when(authService
                .listarSesiones(
                        "cliente@bembos.test"))
                .thenReturn(
                        java.util.List.of());

        mockMvc.perform(
                        get("/api/auth/sessions")
                                .principal(
                                        () ->
                                                "cliente@bembos.test"))
                .andExpect(
                        status().isOk());

        verify(authService)
                .listarSesiones(
                        "cliente@bembos.test");
    }

    @Test
    void debeRevocarSesionPropiaPorId()
            throws Exception {

        mockMvc.perform(
                        delete(
                                "/api/auth/sessions/{idSesionUsuario}",
                                10L)
                                .principal(
                                        () ->
                                                "cliente@bembos.test"))
                .andExpect(
                        status().isNoContent());

        verify(authService)
                .revocarSesion(
                        "cliente@bembos.test",
                        10L);
    }

    @Test
    void debeResponder404AlRevocarSesionNoAccesible()
            throws Exception {

        org.mockito.Mockito
                .doThrow(
                        new com.bembosevents.exception
                                .RecursoNoEncontradoException(
                                        "Sesion no encontrada"))
                .when(authService)
                .revocarSesion(
                        "cliente@bembos.test",
                        99L);

        mockMvc.perform(
                        delete(
                                "/api/auth/sessions/{idSesionUsuario}",
                                99L)
                                .principal(
                                        () ->
                                                "cliente@bembos.test"))
                .andExpect(
                        status().isNotFound())
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Sesion no encontrada"));
    }
}
