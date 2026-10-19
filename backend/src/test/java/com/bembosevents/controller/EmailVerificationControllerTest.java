package com.bembosevents.controller;

import com.bembosevents.dto.auth.EmailVerificationRequest;
import com.bembosevents.dto.auth.EmailVerificationTokenRequest;
import com.bembosevents.dto.common.MensajeResponse;
import com.bembosevents.service.EmailVerificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailVerificationControllerTest {

    @Mock
    private EmailVerificationService
            emailVerificationService;

    @Test
    void reenviarRetornaMensajeGenerico() {

        EmailVerificationController controller =
                new EmailVerificationController(
                        emailVerificationService);

        ResponseEntity<MensajeResponse> response =
                controller.reenviar(
                        new EmailVerificationRequest(
                                "cliente@bembos.test"));

        assertEquals(
                200,
                response.getStatusCode().value());

        assertEquals(
                "Si el correo requiere verificacion, se enviaran instrucciones",
                response.getBody().mensaje());

        verify(emailVerificationService)
                .solicitarReenvio(
                        "cliente@bembos.test");
    }

    @Test
    void verificarInvocaServicio() {

        EmailVerificationController controller =
                new EmailVerificationController(
                        emailVerificationService);

        ResponseEntity<MensajeResponse> response =
                controller.verificar(
                        new EmailVerificationTokenRequest(
                                "raw-token"));

        assertEquals(
                200,
                response.getStatusCode().value());

        assertEquals(
                "Correo verificado correctamente",
                response.getBody().mensaje());

        verify(emailVerificationService)
                .verificar(
                        "raw-token");
    }
}
