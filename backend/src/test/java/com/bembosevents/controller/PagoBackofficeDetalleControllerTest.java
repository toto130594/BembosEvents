package com.bembosevents.controller;

import com.bembosevents.dto.pago.PagoResponse;
import com.bembosevents.service.PagoBackofficeService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

class PagoBackofficeDetalleControllerTest {

    @Test
    void obtenerPagoRetornaOkYDelegaAlService() {

        PagoBackofficeService service =
                mock(PagoBackofficeService.class);

        Authentication authentication =
                mock(Authentication.class);

        PagoBackofficeController controller =
                new PagoBackofficeController(
                        service);

        PagoResponse esperado =
                new PagoResponse(
                        30,
                        200,
                        new BigDecimal("100.00"),
                        "PRUEBA",
                        "PENDIENTE",
                        null,
                        null,
                        LocalDateTime.now());

        when(
                service.obtenerAccesible(
                        30,
                        authentication)
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
                service)
                .obtenerAccesible(
                        30,
                        authentication);
    }

    @Test
    void obtenerPagoUsaGetMappingConIdPago()
            throws Exception {

        Method method =
                PagoBackofficeController.class
                        .getMethod(
                                "obtenerPago",
                                Integer.class,
                                Authentication.class);

        GetMapping annotation =
                method.getAnnotation(
                        GetMapping.class);

        assertNotNull(
                annotation);

        assertEquals(
                "/{idPago}",
                annotation.value()[0]);
    }
}