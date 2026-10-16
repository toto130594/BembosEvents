package com.bembosevents.controller;

import com.bembosevents.dto.auth.AuthResponse;
import com.bembosevents.dto.auth.LoginRequest;
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
}