package com.bembosevents.controller;

import com.bembosevents.dto.mfa.MfaActivateResponse;
import com.bembosevents.dto.mfa.MfaSetupResponse;
import com.bembosevents.exception.GlobalExceptionHandler;
import com.bembosevents.service.MfaEnrollmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MfaControllerTest {

    @Mock
    private MfaEnrollmentService
            mfaEnrollmentService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {

        MfaController controller =
                new MfaController(
                        mfaEnrollmentService);

        LocalValidatorFactoryBean validator =
                new LocalValidatorFactoryBean();

        validator.afterPropertiesSet();

        mockMvc =
                MockMvcBuilders
                        .standaloneSetup(
                                controller)
                        .setControllerAdvice(
                                new GlobalExceptionHandler())
                        .setValidator(
                                validator)
                        .build();
    }

    @Test
    void setupUsaUsuarioAutenticado()
            throws Exception {

        Authentication authentication =
                authentication();

        when(mfaEnrollmentService
                .setup(
                        "cliente@bembos.test"))
                .thenReturn(
                        new MfaSetupResponse(
                                "JBSWY3DPEHPK3PXP",
                                "otpauth://totp/test",
                                false));

        mockMvc.perform(
                        post("/api/mfa/setup")
                                .principal(
                                        authentication))
                .andExpect(
                        status().isOk())
                .andExpect(
                        jsonPath("$.secret")
                                .value(
                                        "JBSWY3DPEHPK3PXP"))
                .andExpect(
                        jsonPath("$.otpauthUri")
                                .value(
                                        "otpauth://totp/test"))
                .andExpect(
                        jsonPath("$.mfaHabilitado")
                                .value(false));

        verify(mfaEnrollmentService)
                .setup(
                        "cliente@bembos.test");
    }

    @Test
    void activateRetornaRecoveryCodes()
            throws Exception {

        Authentication authentication =
                authentication();

        when(mfaEnrollmentService
                .activate(
                        "cliente@bembos.test",
                        "123456"))
                .thenReturn(
                        new MfaActivateResponse(
                                true,
                                List.of(
                                        "AAAA-BBBB-CCCC-DDDD",
                                        "EEEE-FFFF-GGGG-HHHH")));

        String json = """
                {
                  "code": "123456"
                }
                """;

        mockMvc.perform(
                        post("/api/mfa/activate")
                                .principal(
                                        authentication)
                                .contentType(
                                        MediaType.APPLICATION_JSON)
                                .content(
                                        json))
                .andExpect(
                        status().isOk())
                .andExpect(
                        jsonPath("$.mfaHabilitado")
                                .value(true))
                .andExpect(
                        jsonPath("$.recoveryCodes.length()")
                                .value(2))
                .andExpect(
                        jsonPath("$.recoveryCodes[0]")
                                .value(
                                        "AAAA-BBBB-CCCC-DDDD"));

        verify(mfaEnrollmentService)
                .activate(
                        "cliente@bembos.test",
                        "123456");
    }

    @Test
    void activateRechazaCodigoConFormatoInvalido()
            throws Exception {

        Authentication authentication =
                authentication();

        String json = """
                {
                  "code": "12AB"
                }
                """;

        mockMvc.perform(
                        post("/api/mfa/activate")
                                .principal(
                                        authentication)
                                .contentType(
                                        MediaType.APPLICATION_JSON)
                                .content(
                                        json))
                .andExpect(
                        status().isBadRequest());

        verifyNoInteractions(
                mfaEnrollmentService);
    }

    @Test
    void setupSinAutenticacionEsRechazado()
            throws Exception {

        mockMvc.perform(
                        post("/api/mfa/setup"))
                .andExpect(
                        status().isUnauthorized());
    }

    @Test
    void activateSinAutenticacionEsRechazado()
            throws Exception {

        String json = """
                {
                  "code": "123456"
                }
                """;

        mockMvc.perform(
                        post("/api/mfa/activate")
                                .contentType(
                                        MediaType.APPLICATION_JSON)
                                .content(
                                        json))
                .andExpect(
                        status().isUnauthorized());

        verifyNoInteractions(
                mfaEnrollmentService);
    }

    private Authentication authentication() {

        return new UsernamePasswordAuthenticationToken(
                "cliente@bembos.test",
                "N/A",
                List.of());
    }
}