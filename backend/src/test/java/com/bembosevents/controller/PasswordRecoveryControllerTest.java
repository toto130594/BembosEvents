package com.bembosevents.controller;

import com.bembosevents.dto.auth.PasswordRecoveryRequest;
import com.bembosevents.dto.auth.PasswordResetRequest;
import com.bembosevents.dto.common.MensajeResponse;
import com.bembosevents.service.PasswordRecoveryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PasswordRecoveryControllerTest {

    @Mock
    private PasswordRecoveryService
            passwordRecoveryService;

    @Test
    void solicitarSiempreRetornaMensajeGenerico() {

        PasswordRecoveryController controller =
                new PasswordRecoveryController(
                        passwordRecoveryService);

        ResponseEntity<MensajeResponse> response =
                controller.solicitar(
                        new PasswordRecoveryRequest(
                                "cliente@bembos.test"));

        assertEquals(
                200,
                response.getStatusCode().value());

        assertEquals(
                "Si el correo existe, se enviaran instrucciones para restablecer la contrasena",
                response.getBody().mensaje());

        verify(passwordRecoveryService)
                .solicitar(
                        "cliente@bembos.test");
    }

    @Test
    void restablecerInvocaServicio() {

        PasswordRecoveryController controller =
                new PasswordRecoveryController(
                        passwordRecoveryService);

        ResponseEntity<MensajeResponse> response =
                controller.restablecer(
                        new PasswordResetRequest(
                                "raw-token",
                                "NuevaPassword123"));

        assertEquals(
                200,
                response.getStatusCode().value());

        assertEquals(
                "Contrasena restablecida correctamente",
                response.getBody().mensaje());

        verify(passwordRecoveryService)
                .restablecer(
                        "raw-token",
                        "NuevaPassword123");
    }
}
