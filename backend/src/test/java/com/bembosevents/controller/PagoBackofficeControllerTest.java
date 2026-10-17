package com.bembosevents.controller;

import com.bembosevents.dto.pago.PagoResponse;
import com.bembosevents.service.PagoBackofficeService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

class PagoBackofficeControllerTest {

    @Test
    void listarPagosRetornaOkYDelega() {

        PagoBackofficeService service =
                mock(PagoBackofficeService.class);

        Authentication authentication =
                mock(Authentication.class);

        PagoBackofficeController controller =
                new PagoBackofficeController(
                        service);

        PagoResponse pago =
                new PagoResponse(
                        10,
                        20,
                        new BigDecimal("100.00"),
                        "PRUEBA",
                        "PENDIENTE",
                        null,
                        null,
                        LocalDateTime.now());

        when(
                service.listarAccesibles(
                        authentication)
        ).thenReturn(
                List.of(pago));

        ResponseEntity<List<PagoResponse>> response =
                controller.listarPagos(
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
                service)
                .listarAccesibles(
                        authentication);
    }

    @Test
    void controllerRestringeRolesBackoffice() {

        PreAuthorize annotation =
                PagoBackofficeController.class
                        .getAnnotation(
                                PreAuthorize.class);

        assertNotNull(
                annotation);

        assertEquals(
                "hasAnyRole('ADMINISTRADOR','ENCARGADO_LOCAL')",
                annotation.value());
    }

    @Test
    void listarPagosUsaGetMapping()
            throws Exception {

        Method method =
                PagoBackofficeController.class
                        .getMethod(
                                "listarPagos",
                                Authentication.class);

        assertNotNull(
                method.getAnnotation(
                        GetMapping.class));
    }
}