package com.bembosevents.controller;

import com.bembosevents.dto.pago.PagoResponse;
import com.bembosevents.service.PagoService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PagoClienteListadoControllerTest {

    @Test
    void listarPagosRetornaOkYDelegaAlService() {

        PagoService pagoService =
                mock(PagoService.class);

        Authentication authentication =
                mock(Authentication.class);

        PagoClienteController controller =
                new PagoClienteController(
                        pagoService);

        PagoResponse pago =
                new PagoResponse(
                        1,
                        20,
                        new BigDecimal("100.00"),
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
                pagoService.listarPagosPropios(
                        "cliente@bembos.test",
                        20))
                .thenReturn(
                        List.of(pago));

        ResponseEntity<List<PagoResponse>> response =
                controller.listarPagos(
                        20,
                        authentication);

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode());

        assertNotNull(
                response.getBody());

        assertEquals(
                1,
                response.getBody().size());

        verify(
                pagoService)
                .listarPagosPropios(
                        "cliente@bembos.test",
                        20);
    }

    @Test
    void listarPagosUsaGetMapping() throws Exception {

        Method method =
                PagoClienteController.class
                        .getMethod(
                                "listarPagos",
                                Integer.class,
                                Authentication.class);

        assertNotNull(
                method.getAnnotation(
                        GetMapping.class));
    }
}