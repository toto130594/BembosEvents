package com.bembosevents.controller;

import com.bembosevents.domain.EstadoReserva;
import com.bembosevents.exception.GlobalExceptionHandler;
import com.bembosevents.service.ReservaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ReservaControllerTest {

    @Mock
    private ReservaService reservaService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {

        ReservaController controller =
                new ReservaController(reservaService);

        LocalValidatorFactoryBean validator =
                new LocalValidatorFactoryBean();

        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(
                        new GlobalExceptionHandler())
                .setValidator(validator)
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @Test
    void debeValidarCapacidad() throws Exception {

        String json = """
                {
                  "idSalon": 1,
                  "cantidadInvitados": 40
                }
                """;

        mockMvc.perform(
                        post("/api/reservas/validar-capacidad")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.mensaje")
                                .value("Capacidad valida"));

        verify(reservaService)
                .validarCapacidad(1, 40);
    }

    @Test
    void debeRechazarCantidadDeInvitadosInvalida()
            throws Exception {

        String json = """
                {
                  "idSalon": 1,
                  "cantidadInvitados": 0
                }
                """;

        mockMvc.perform(
                        post("/api/reservas/validar-capacidad")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath(
                                "$.validationErrors.cantidadInvitados")
                                .exists());

        verifyNoInteractions(reservaService);
    }

    @Test
    void debeValidarDisponibilidad() throws Exception {

        String json = """
                {
                  "idSalon": 1,
                  "fechaEvento": "2026-10-20",
                  "horaInicio": "14:00",
                  "horaFin": "18:00"
                }
                """;

        mockMvc.perform(
                        post("/api/reservas/validar-disponibilidad")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.mensaje")
                                .value("Salon disponible"));

        verify(reservaService)
                .validarDisponibilidad(
                        1,
                        LocalDate.of(2026, 10, 20),
                        LocalTime.of(14, 0),
                        LocalTime.of(18, 0));
    }

    @Test
    void debeResponderBadRequestCuandoHaySolapamiento()
            throws Exception {

        doThrow(
                new IllegalArgumentException(
                        "El salon no esta disponible en el horario solicitado"))
                .when(reservaService)
                .validarDisponibilidad(
                        eq(1),
                        any(LocalDate.class),
                        any(LocalTime.class),
                        any(LocalTime.class));

        String json = """
                {
                  "idSalon": 1,
                  "fechaEvento": "2026-10-20",
                  "horaInicio": "14:00",
                  "horaFin": "18:00"
                }
                """;

        mockMvc.perform(
                        post("/api/reservas/validar-disponibilidad")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "El salon no esta disponible en el horario solicitado"));
    }

    @Test
    void debeValidarMontoMinimo() throws Exception {

        String json = """
                {
                  "montoTotal": 500.00
                }
                """;

        mockMvc.perform(
                        post("/api/reservas/validar-monto-minimo")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isOk());

        verify(reservaService)
                .validarMontoMinimo(
                        new BigDecimal("500.00"));
    }

    @Test
    void debeCalcularMontoTotal() throws Exception {

        when(reservaService.calcularMontoTotal(
                any(BigDecimal.class),
                any(BigDecimal.class)))
                .thenReturn(
                        new BigDecimal("650.00"));

        String json = """
                {
                  "montoBase": 500.00,
                  "montoServicios": 150.00
                }
                """;

        mockMvc.perform(
                        post("/api/reservas/calcular-monto-total")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.monto")
                                .value(650.00));
    }

    @Test
    void debeCalcularDeposito() throws Exception {

        when(reservaService.calcularMontoDeposito(
                any(BigDecimal.class)))
                .thenReturn(
                        new BigDecimal("250.00"));

        String json = """
                {
                  "montoTotal": 500.00
                }
                """;

        mockMvc.perform(
                        post("/api/reservas/calcular-deposito")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.monto")
                                .value(250.00));
    }

    @Test
    void debeValidarTransicionReserva() throws Exception {

        String json = """
                {
                  "estadoActual": "PENDIENTE",
                  "nuevoEstado": "CONFIRMADA"
                }
                """;

        mockMvc.perform(
                        post("/api/reservas/validar-transicion")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isOk());

        verify(reservaService)
                .validarTransicionEstado(
                        EstadoReserva.PENDIENTE,
                        EstadoReserva.CONFIRMADA);
    }

    @Test
    void debeRechazarEstadoDesconocido() throws Exception {

        String json = """
                {
                  "estadoActual": "ESTADO_INVENTADO",
                  "nuevoEstado": "CONFIRMADA"
                }
                """;

        mockMvc.perform(
                        post("/api/reservas/validar-transicion")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.error")
                                .value("Invalid JSON"));
    }
}