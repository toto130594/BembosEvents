package com.bembosevents.controller;

import com.bembosevents.dto.pago.CrearPagoRequest;
import com.bembosevents.dto.pago.PagoResponse;
import com.bembosevents.service.PagoService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PagoClienteControllerTest {

    @Test
    void iniciarPagoRetornaCreatedYDelegaAlService() {

        PagoService pagoService =
                mock(PagoService.class);

        Authentication authentication =
                mock(Authentication.class);

        PagoClienteController controller =
                new PagoClienteController(
                        pagoService);

        CrearPagoRequest request =
                new CrearPagoRequest(
                        new BigDecimal("250.00"),
                        "PRUEBA");

        PagoResponse esperado =
                new PagoResponse(
                        30,
                        20,
                        new BigDecimal("250.00"),
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
                pagoService.iniciarPago(
                        "cliente@bembos.test",
                        20,
                        request))
                .thenReturn(
                        esperado);

        ResponseEntity<PagoResponse> response =
                controller.iniciarPago(
                        20,
                        request,
                        authentication);

        assertEquals(
                HttpStatus.CREATED,
                response.getStatusCode());

        assertEquals(
                esperado,
                response.getBody());

        verify(
                pagoService)
                .iniciarPago(
                        "cliente@bembos.test",
                        20,
                        request);
    }

    @Test
    void controllerRestringeAccesoACliente() {

        PreAuthorize annotation =
                PagoClienteController.class
                        .getAnnotation(
                                PreAuthorize.class);

        assertNotNull(
                annotation);

        assertEquals(
                "hasRole('CLIENTE')",
                annotation.value());
    }

    @Test
    void iniciarPagoUsaPostMapping() throws Exception {

        Method method =
                PagoClienteController.class
                        .getMethod(
                                "iniciarPago",
                                Integer.class,
                                CrearPagoRequest.class,
                                Authentication.class);

        PostMapping annotation =
                method.getAnnotation(
                        PostMapping.class);

        assertNotNull(
                annotation);
    }
}