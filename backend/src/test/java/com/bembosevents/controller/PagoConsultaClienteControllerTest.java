package com.bembosevents.controller;

import com.bembosevents.dto.pago.PagoResponse;
import com.bembosevents.service.PagoService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PagoConsultaClienteControllerTest {

    @Test
    void obtenerPagoRetornaOkYDelegaAlService() {

        PagoService pagoService =
                mock(PagoService.class);

        Authentication authentication =
                mock(Authentication.class);

        PagoConsultaClienteController controller =
                new PagoConsultaClienteController(
                        pagoService);

        PagoResponse esperado =
                new PagoResponse(
                        30,
                        20,
                        new BigDecimal("150.00"),
                        "PRUEBA",
                        "PENDIENTE",
                        null,
                        null,
                        LocalDateTime.now());

        when(
                authentication.getName())
                .thenReturn(
                        "cliente@bembos.test");

        when(
                pagoService.obtenerPagoPropio(
                        "cliente@bembos.test",
                        30)
        ).thenReturn(
                esperado);

        ResponseEntity<PagoResponse> response =
                controller.obtenerPago(
                        30,
                        authentication);

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode());

        assertEquals(
                esperado,
                response.getBody());

        verify(
                pagoService)
                .obtenerPagoPropio(
                        "cliente@bembos.test",
                        30);
    }

    @Test
    void controllerEsSoloCliente() {

        PreAuthorize annotation =
                PagoConsultaClienteController.class
                        .getAnnotation(
                                PreAuthorize.class);

        assertNotNull(annotation);

        assertEquals(
                "hasRole('CLIENTE')",
                annotation.value());
    }

    @Test
    void obtenerPagoUsaGetMapping()
            throws Exception {

        Method method =
                PagoConsultaClienteController.class
                        .getMethod(
                                "obtenerPago",
                                Integer.class,
                                Authentication.class);

        GetMapping annotation =
                method.getAnnotation(
                        GetMapping.class);

        assertNotNull(annotation);

        assertEquals(
                "/{idPago}",
                annotation.value()[0]);
    }
}