package com.bembosevents.controller;

import com.bembosevents.dto.auth.AuthResponse;
import com.bembosevents.service.MfaLoginVerificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MfaLoginControllerTest {

    @Mock
    private MfaLoginVerificationService
            mfaLoginVerificationService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {

        MfaLoginController controller =
                new MfaLoginController(
                        mfaLoginVerificationService);

        LocalValidatorFactoryBean validator =
                new LocalValidatorFactoryBean();

        validator.afterPropertiesSet();

        mockMvc =
                MockMvcBuilders
                        .standaloneSetup(controller)
                        .setValidator(validator)
                        .build();
    }

    @Test
    void debeVerificarTotpYRetornarJwt()
            throws Exception {

        when(mfaLoginVerificationService
                .verifyTotp(
                        "challenge-raw",
                        "123456"))
                .thenReturn(
                        new AuthResponse(
                                "jwt-final",
                                "Bearer",
                                3600000L));

        String json = """
                {
                  "challengeToken": "challenge-raw",
                  "code": "123456"
                }
                """;

        mockMvc.perform(
                        post("/api/auth/mfa/verificar")
                                .contentType(
                                        MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(
                        status().isOk())
                .andExpect(
                        jsonPath("$.token")
                                .value("jwt-final"));

        verify(mfaLoginVerificationService)
                .verifyTotp(
                        "challenge-raw",
                        "123456");
    }

    @Test
    void debeAceptarRecoveryCodeYRetornarJwt()
            throws Exception {

        when(mfaLoginVerificationService
                .verifyRecovery(
                        "challenge-raw",
                        "AAAA-BBBB-CCCC-DDDD"))
                .thenReturn(
                        new AuthResponse(
                                "jwt-final",
                                "Bearer",
                                3600000L));

        String json = """
                {
                  "challengeToken": "challenge-raw",
                  "recoveryCode": "AAAA-BBBB-CCCC-DDDD"
                }
                """;

        mockMvc.perform(
                        post("/api/auth/mfa/recuperacion")
                                .contentType(
                                        MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(
                        status().isOk())
                .andExpect(
                        jsonPath("$.token")
                                .value("jwt-final"))
                .andExpect(
                        jsonPath("$.mfaRequired")
                                .value(false));

        verify(mfaLoginVerificationService)
                .verifyRecovery(
                        "challenge-raw",
                        "AAAA-BBBB-CCCC-DDDD");
    }

    @Test
    void recoveryVacioEsRechazado()
            throws Exception {

        String json = """
                {
                  "challengeToken": "challenge-raw",
                  "recoveryCode": ""
                }
                """;

        mockMvc.perform(
                        post("/api/auth/mfa/recuperacion")
                                .contentType(
                                        MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(
                        status().isBadRequest());

        verifyNoInteractions(
                mfaLoginVerificationService);
    }
}