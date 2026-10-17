package com.bembosevents.controller;

import com.bembosevents.dto.reserva.CrearReservaRequest;
import com.bembosevents.dto.reserva.ReservaCreadaResponse;
import com.bembosevents.dto.reserva.ReservaDetalleResponse;
import com.bembosevents.dto.reserva.ReservaDetalleServicioResponse;
import com.bembosevents.dto.reserva.ReservaResumenResponse;
import com.bembosevents.exception.ConflictoReservaException;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @Test
    void debeListarReservasDelClienteAutenticado()
            throws Exception {

        ReservaResumenResponse response =
                new ReservaResumenResponse(
                        101,
                        "BEM-2026-000101",
                        "PENDIENTE",
                        1,
                        10,
                        null,
                        null,
                        LocalDate.of(
                                2026,
                                12,
                                20),
                        LocalTime.of(
                                14,
                                0),
                        LocalTime.of(
                                18,
                                0),
                        40,
                        new BigDecimal("500.00"),
                        new BigDecimal("250.00"),
                        LocalDateTime.of(
                                2026,
                                10,
                                16,
                                16,
                                0));

        when(reservaClienteService
                .misReservas(CORREO))
                .thenReturn(
                        List.of(response));

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        CORREO,
                        null);

        mockMvc.perform(
                        get("/api/reservas/mias")
                                .principal(authentication))
                .andExpect(
                        status().isOk())
                .andExpect(
                        jsonPath("$[0].idReserva")
                                .value(101))
                .andExpect(
                        jsonPath("$[0].codigoReserva")
                                .value(
                                        "BEM-2026-000101"))
                .andExpect(
                        jsonPath("$[0].estado")
                                .value("PENDIENTE"))
                .andExpect(
                        jsonPath("$[0].idSede")
                                .value(1))
                .andExpect(
                        jsonPath("$[0].idSalon")
                                .value(10))
                .andExpect(
                        jsonPath("$[0].cantidadInvitados")
                                .value(40))
                .andExpect(
                        jsonPath("$[0].montoTotal")
                                .value(500.00))
                .andExpect(
                        jsonPath("$[0].montoDeposito")
                                .value(250.00));

        verify(reservaClienteService)
                .misReservas(CORREO);
    }

    @Test
    void debeConsultarReservaPropiaDelClienteAutenticado()
            throws Exception {

        ReservaDetalleServicioResponse servicio =
                new ReservaDetalleServicioResponse(
                        7,
                        2,
                        new BigDecimal("125.00"),
                        new BigDecimal("250.00"));

        ReservaDetalleResponse response =
                new ReservaDetalleResponse(
                        150,
                        "BEM-2026-000150",
                        "PENDIENTE",
                        1,
                        10,
                        null,
                        null,
                        LocalDate.of(
                                2026,
                                12,
                                20),
                        LocalTime.of(
                                14,
                                0),
                        LocalTime.of(
                                18,
                                0),
                        40,
                        new BigDecimal("500.00"),
                        new BigDecimal("250.00"),
                        new BigDecimal("750.00"),
                        new BigDecimal("0.50"),
                        new BigDecimal("375.00"),
                        List.of(servicio),
                        LocalDateTime.of(
                                2026,
                                10,
                                16,
                                17,
                                0),
                        LocalDateTime.of(
                                2026,
                                10,
                                16,
                                17,
                                0));

        when(reservaClienteService
                .consultarPropia(
                        CORREO,
                        150))
                .thenReturn(response);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        CORREO,
                        null);

        mockMvc.perform(
                        get("/api/reservas/150")
                                .principal(authentication))
                .andExpect(
                        status().isOk())
                .andExpect(
                        jsonPath("$.idReserva")
                                .value(150))
                .andExpect(
                        jsonPath("$.codigoReserva")
                                .value(
                                        "BEM-2026-000150"))
                .andExpect(
                        jsonPath("$.estado")
                                .value("PENDIENTE"))
                .andExpect(
                        jsonPath("$.montoTotal")
                                .value(750.00))
                .andExpect(
                        jsonPath("$.montoDeposito")
                                .value(375.00))
                .andExpect(
                        jsonPath("$.serviciosAdicionales[0].idServicioAdicional")
                                .value(7))
                .andExpect(
                        jsonPath("$.serviciosAdicionales[0].cantidad")
                                .value(2))
                .andExpect(
                        jsonPath("$.serviciosAdicionales[0].precioUnitario")
                                .value(125.00))
                .andExpect(
                        jsonPath("$.serviciosAdicionales[0].subtotal")
                                .value(250.00));

        verify(reservaClienteService)
                .consultarPropia(
                        CORREO,
                        150);
    }

    @Test
    void debeCancelarReservaPropiaDelClienteAutenticado()
            throws Exception {

        ReservaDetalleResponse response =
                new ReservaDetalleResponse(
                        150,
                        "BEM-2026-000150",
                        "CANCELADA",
                        1,
                        10,
                        null,
                        null,
                        LocalDate.of(
                                2026,
                                12,
                                20),
                        LocalTime.of(
                                14,
                                0),
                        LocalTime.of(
                                18,
                                0),
                        40,
                        new BigDecimal("500.00"),
                        new BigDecimal("250.00"),
                        new BigDecimal("750.00"),
                        new BigDecimal("0.50"),
                        new BigDecimal("375.00"),
                        List.of(),
                        LocalDateTime.of(
                                2026,
                                10,
                                16,
                                17,
                                0),
                        LocalDateTime.of(
                                2026,
                                10,
                                16,
                                19,
                                0));

        when(reservaClienteService
                .cancelarPropia(
                        CORREO,
                        150))
                .thenReturn(response);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        CORREO,
                        null);

        mockMvc.perform(
                        post("/api/reservas/150/cancelacion")
                                .principal(authentication))
                .andExpect(
                        status().isOk())
                .andExpect(
                        jsonPath("$.idReserva")
                                .value(150))
                .andExpect(
                        jsonPath("$.codigoReserva")
                                .value("BEM-2026-000150"))
                .andExpect(
                        jsonPath("$.estado")
                                .value("CANCELADA"));

        verify(reservaClienteService)
                .cancelarPropia(
                        CORREO,
                        150);
    }

    @Test
    void debeResponderConflictCuandoReservaNoPuedeCancelarse()
            throws Exception {

        when(reservaClienteService
                .cancelarPropia(
                        CORREO,
                        150))
                .thenThrow(
                        new ConflictoReservaException(
                                "La reserva no puede cancelarse en su estado actual"));

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        CORREO,
                        null);

        mockMvc.perform(
                        post("/api/reservas/150/cancelacion")
                                .principal(authentication))
                .andExpect(
                        status().isConflict());

        verify(reservaClienteService)
                .cancelarPropia(
                        CORREO,
                        150);
    }
}