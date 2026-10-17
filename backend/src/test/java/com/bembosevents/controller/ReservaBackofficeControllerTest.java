package com.bembosevents.controller;

import com.bembosevents.dto.reserva.CambiarEstadoReservaRequest;
import com.bembosevents.dto.reserva.ReservaDetalleResponse;
import com.bembosevents.dto.reserva.ReservaResumenResponse;
import com.bembosevents.exception.ConflictoReservaException;
import com.bembosevents.exception.GlobalExceptionHandler;
import com.bembosevents.service.ReservaBackofficeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ReservaBackofficeControllerTest {

    private static final String CORREO =
            "admin@bembos.test";

    @Mock
    private ReservaBackofficeService reservaBackofficeService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {

        ReservaBackofficeController controller =
                new ReservaBackofficeController(
                        reservaBackofficeService);

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
    void debeListarReservasAccesibles()
            throws Exception {

        ReservaResumenResponse response =
                resumen(
                        101,
                        "PENDIENTE");

        UsernamePasswordAuthenticationToken authentication =
                autenticacion();

        when(reservaBackofficeService
                .listarAccesibles(authentication))
                .thenReturn(
                        List.of(response));

        mockMvc.perform(
                        get("/api/backoffice/reservas")
                                .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$[0].idReserva")
                                .value(101))
                .andExpect(
                        jsonPath("$[0].estado")
                                .value("PENDIENTE"));

        verify(reservaBackofficeService)
                .listarAccesibles(authentication);
    }

    @Test
    void debeConsultarDetalleDeReservaBackoffice()
            throws Exception {

        ReservaDetalleResponse response =
                detalle(
                        150,
                        "PENDIENTE");

        UsernamePasswordAuthenticationToken authentication =
                autenticacion();

        when(reservaBackofficeService
                .obtenerAccesible(
                        150,
                        authentication))
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/backoffice/reservas/150")
                                .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.idReserva")
                                .value(150))
                .andExpect(
                        jsonPath("$.estado")
                                .value("PENDIENTE"))
                .andExpect(
                        jsonPath("$.idSede")
                                .value(1));

        verify(reservaBackofficeService)
                .obtenerAccesible(
                        150,
                        authentication);
    }

    @Test
    void debeCambiarEstadoDeReservaBackoffice()
            throws Exception {

        ReservaDetalleResponse response =
                detalle(
                        150,
                        "CONFIRMADA");

        UsernamePasswordAuthenticationToken authentication =
                autenticacion();

        when(reservaBackofficeService
                .cambiarEstado(
                        eq(150),
                        any(CambiarEstadoReservaRequest.class),
                        eq(authentication)))
                .thenReturn(response);

        String json = """
                {
                  "nuevoEstado": "CONFIRMADA"
                }
                """;

        mockMvc.perform(
                        patch("/api/backoffice/reservas/150/estado")
                                .principal(authentication)
                                .contentType(
                                        MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.idReserva")
                                .value(150))
                .andExpect(
                        jsonPath("$.estado")
                                .value("CONFIRMADA"));

        ArgumentCaptor<CambiarEstadoReservaRequest>
                requestCaptor =
                ArgumentCaptor.forClass(
                        CambiarEstadoReservaRequest.class);

        verify(reservaBackofficeService)
                .cambiarEstado(
                        eq(150),
                        requestCaptor.capture(),
                        eq(authentication));

        assertEquals(
                "CONFIRMADA",
                requestCaptor
                        .getValue()
                        .nuevoEstado()
                        .name());
    }

    @Test
    void debeResponderConflictCuandoTransicionNoPermitida()
            throws Exception {

        UsernamePasswordAuthenticationToken authentication =
                autenticacion();

        when(reservaBackofficeService
                .cambiarEstado(
                        eq(150),
                        any(CambiarEstadoReservaRequest.class),
                        eq(authentication)))
                .thenThrow(
                        new ConflictoReservaException(
                                "La transicion de estado de la reserva no esta permitida"));

        String json = """
                {
                  "nuevoEstado": "PENDIENTE"
                }
                """;

        mockMvc.perform(
                        patch("/api/backoffice/reservas/150/estado")
                                .principal(authentication)
                                .contentType(
                                        MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isConflict())
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "La transicion de estado de la reserva no esta permitida"));
    }

    @Test
    void debeRechazarCambioSinNuevoEstado()
            throws Exception {

        UsernamePasswordAuthenticationToken authentication =
                autenticacion();

        mockMvc.perform(
                        patch("/api/backoffice/reservas/150/estado")
                                .principal(authentication)
                                .contentType(
                                        MediaType.APPLICATION_JSON)
                                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath(
                                "$.validationErrors.nuevoEstado")
                                .exists());

        verifyNoInteractions(
                reservaBackofficeService);
    }

    @Test
    void debeRechazarEstadoDesconocido()
            throws Exception {

        UsernamePasswordAuthenticationToken authentication =
                autenticacion();

        String json = """
                {
                  "nuevoEstado": "ESTADO_INVENTADO"
                }
                """;

        mockMvc.perform(
                        patch("/api/backoffice/reservas/150/estado")
                                .principal(authentication)
                                .contentType(
                                        MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.error")
                                .value("Invalid JSON"));

        verifyNoInteractions(
                reservaBackofficeService);
    }

    @Test
    void debeProtegerControllerParaRolesBackoffice() {

        PreAuthorize annotation =
                ReservaBackofficeController.class
                        .getAnnotation(
                                PreAuthorize.class);

        assertNotNull(annotation);

        assertEquals(
                "hasAnyRole('ADMINISTRADOR','ENCARGADO_LOCAL')",
                annotation.value());
    }

    private UsernamePasswordAuthenticationToken autenticacion() {

        return new UsernamePasswordAuthenticationToken(
                CORREO,
                null,
                List.of(
                        new SimpleGrantedAuthority(
                                "ROLE_ADMINISTRADOR")));
    }

    private ReservaResumenResponse resumen(
            Integer idReserva,
            String estado) {

        return new ReservaResumenResponse(
                idReserva,
                "BEM-2026-" + idReserva,
                estado,
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
                new BigDecimal("750.00"),
                new BigDecimal("375.00"),
                LocalDateTime.of(
                        2026,
                        10,
                        16,
                        18,
                        0));
    }

    private ReservaDetalleResponse detalle(
            Integer idReserva,
            String estado) {

        return new ReservaDetalleResponse(
                idReserva,
                "BEM-2026-" + idReserva,
                estado,
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
                        18,
                        0));
    }
}