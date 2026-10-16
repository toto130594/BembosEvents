package com.bembosevents.controller;

import com.bembosevents.dto.reserva.CrearReservaRequest;
import com.bembosevents.dto.reserva.ReservaCreadaResponse;
import com.bembosevents.exception.GlobalExceptionHandler;
import com.bembosevents.service.ReservaClienteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ReservaClienteControllerTest {

    private static final String CORREO =
            "cliente@bembosevents.com";

    @Mock
    private ReservaClienteService reservaClienteService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {

        ReservaClienteController controller =
                new ReservaClienteController(
                        reservaClienteService);

        LocalValidatorFactoryBean validator =
                new LocalValidatorFactoryBean();

        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(
                        new GlobalExceptionHandler())
                .setValidator(validator)
                .build();

        objectMapper =
                new ObjectMapper();

        objectMapper.findAndRegisterModules();
    }

    @Test
    void debeCrearReservaDelClienteAutenticado()
            throws Exception {

        ReservaCreadaResponse response =
                new ReservaCreadaResponse(
                        100,
                        "BEM-2026-000042",
                        "PENDIENTE",
                        1,
                        10,
                        null,
                        null,
                        LocalDate.of(
                                2026,
                                11,
                                20),
                        LocalTime.of(
                                14,
                                0),
                        LocalTime.of(
                                18,
                                0),
                        40,
                        BigDecimal.ZERO,
                        new BigDecimal("250.00"),
                        new BigDecimal("250.00"),
                        new BigDecimal("0.50"),
                        new BigDecimal("125.00"),
                        LocalDateTime.of(
                                2026,
                                10,
                                16,
                                15,
                                0));

        when(reservaClienteService.crear(
                eq(CORREO),
                any(CrearReservaRequest.class)))
                .thenReturn(response);

        String json = """
                {
                  "idSede": 1,
                  "idSalon": 10,
                  "idPaquete": null,
                  "idTematica": null,
                  "fechaEvento": "2026-11-20",
                  "horaInicio": "14:00",
                  "horaFin": "18:00",
                  "cantidadInvitados": 40,
                  "serviciosAdicionales": []
                }
                """;

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        CORREO,
                        null);

        mockMvc.perform(
                        post("/api/reservas")
                                .principal(authentication)
                                .contentType(
                                        MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(
                        status().isCreated())
                .andExpect(
                        jsonPath("$.idReserva")
                                .value(100))
                .andExpect(
                        jsonPath("$.codigoReserva")
                                .value(
                                        "BEM-2026-000042"))
                .andExpect(
                        jsonPath("$.estado")
                                .value("PENDIENTE"))
                .andExpect(
                        jsonPath("$.idSede")
                                .value(1))
                .andExpect(
                        jsonPath("$.idSalon")
                                .value(10))
                .andExpect(
                        jsonPath("$.cantidadInvitados")
                                .value(40))
                .andExpect(
                        jsonPath("$.montoServicios")
                                .value(250.00))
                .andExpect(
                        jsonPath("$.montoTotal")
                                .value(250.00))
                .andExpect(
                        jsonPath("$.montoDeposito")
                                .value(125.00));

        ArgumentCaptor<CrearReservaRequest>
                requestCaptor =
                ArgumentCaptor.forClass(
                        CrearReservaRequest.class);

        verify(reservaClienteService)
                .crear(
                        eq(CORREO),
                        requestCaptor.capture());

        CrearReservaRequest request =
                requestCaptor.getValue();

        assertEquals(
                1,
                request.idSede());

        assertEquals(
                10,
                request.idSalon());

        assertEquals(
                LocalDate.of(
                        2026,
                        11,
                        20),
                request.fechaEvento());

        assertEquals(
                LocalTime.of(
                        14,
                        0),
                request.horaInicio());

        assertEquals(
                LocalTime.of(
                        18,
                        0),
                request.horaFin());

        assertEquals(
                40,
                request.cantidadInvitados());
    }
}