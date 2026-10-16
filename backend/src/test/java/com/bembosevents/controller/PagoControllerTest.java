package com.bembosevents.controller;

import com.bembosevents.domain.EstadoPago;
import com.bembosevents.exception.GlobalExceptionHandler;
import com.bembosevents.service.PagoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PagoControllerTest {

    @Mock
    private PagoService pagoService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {

        PagoController controller =
                new PagoController(pagoService);

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
    void debeValidarTransicionPago() throws Exception {

        String json = """
                {
                  "estadoActual": "PENDIENTE",
                  "nuevoEstado": "APROBADO"
                }
                """;

        mockMvc.perform(
                        post("/api/pagos/validar-transicion")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.mensaje")
                                .value(
                                        "Transicion de pago valida"));

        verify(pagoService)
                .validarTransicionEstado(
                        EstadoPago.PENDIENTE,
                        EstadoPago.APROBADO);
    }

    @Test
    void debeResponderBadRequestAnteTransicionInvalida()
            throws Exception {

        doThrow(
                new IllegalArgumentException(
                        "Transicion de estado de pago no permitida"))
                .when(pagoService)
                .validarTransicionEstado(
                        EstadoPago.REEMBOLSADO,
                        EstadoPago.PENDIENTE);

        String json = """
                {
                  "estadoActual": "REEMBOLSADO",
                  "nuevoEstado": "PENDIENTE"
                }
                """;

        mockMvc.perform(
                        post("/api/pagos/validar-transicion")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Transicion de estado de pago no permitida"));
    }

    @Test
    void debeRechazarEstadoPagoDesconocido()
            throws Exception {

        String json = """
                {
                  "estadoActual": "INVENTADO",
                  "nuevoEstado": "APROBADO"
                }
                """;

        mockMvc.perform(
                        post("/api/pagos/validar-transicion")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.error")
                                .value("Invalid JSON"));
    }
}