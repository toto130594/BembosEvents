package com.bembosevents.service.impl;

import com.bembosevents.dto.pago.PagoResponse;
import com.bembosevents.entity.Pago;
import com.bembosevents.entity.Reserva;
import com.bembosevents.exception.RecursoNoEncontradoException;
import com.bembosevents.repository.PagoRepository;
import com.bembosevents.service.SedeAccessService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PagoBackofficeDetalleServiceTest {

    @Mock
    private PagoRepository pagoRepository;

    @Mock
    private SedeAccessService sedeAccessService;

    @Mock
    private Authentication authentication;

    private PagoBackofficeServiceImpl service;

    @BeforeEach
    void setUp() {

        service =
                new PagoBackofficeServiceImpl(
                        pagoRepository,
                        sedeAccessService);
    }

    @Test
    void administradorPuedeConsultarCualquierPago() {

        Pago pago =
                crearPago(
                        30,
                        200);

        when(
                sedeAccessService.esAdministrador(
                        authentication)
        ).thenReturn(
                true);

        when(
                pagoRepository.findById(
                        30)
        ).thenReturn(
                Optional.of(pago));

        PagoResponse response =
                service.obtenerAccesible(
                        30,
                        authentication);

        assertEquals(
                30,
                response.idPago());

        assertEquals(
                200,
                response.idReserva());

        verify(
                pagoRepository)
                .findById(
                        30);

        verify(
                pagoRepository,
                never())
                .findByIdPagoAndReserva_Sede_IdSede(
                        org.mockito.ArgumentMatchers.anyInt(),
                        org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    void encargadoPuedeConsultarPagoDeSuSede() {

        Pago pago =
                crearPago(
                        40,
                        300);

        when(
                sedeAccessService.esAdministrador(
                        authentication)
        ).thenReturn(
                false);

        when(
                sedeAccessService.esEncargadoLocal(
                        authentication)
        ).thenReturn(
                true);

        when(
                authentication.getName()
        ).thenReturn(
                "encargado@bembos.test");

        when(
                sedeAccessService.obtenerSedeAsignada(
                        "encargado@bembos.test")
        ).thenReturn(
                5);

        when(
                pagoRepository
                        .findByIdPagoAndReserva_Sede_IdSede(
                                40,
                                5)
        ).thenReturn(
                Optional.of(pago));

        PagoResponse response =
                service.obtenerAccesible(
                        40,
                        authentication);

        assertEquals(
                40,
                response.idPago());

        verify(
                pagoRepository)
                .findByIdPagoAndReserva_Sede_IdSede(
                        40,
                        5);

        verify(
                pagoRepository,
                never())
                .findById(
                        40);
    }

    @Test
    void encargadoNoPuedeVerPagoDeOtraSede() {

        when(
                sedeAccessService.esAdministrador(
                        authentication)
        ).thenReturn(
                false);

        when(
                sedeAccessService.esEncargadoLocal(
                        authentication)
        ).thenReturn(
                true);

        when(
                authentication.getName()
        ).thenReturn(
                "encargado@bembos.test");

        when(
                sedeAccessService.obtenerSedeAsignada(
                        "encargado@bembos.test")
        ).thenReturn(
                5);

        when(
                pagoRepository
                        .findByIdPagoAndReserva_Sede_IdSede(
                                99,
                                5)
        ).thenReturn(
                Optional.empty());

        RecursoNoEncontradoException exception =
                assertThrows(
                        RecursoNoEncontradoException.class,
                        () ->
                                service.obtenerAccesible(
                                        99,
                                        authentication));

        assertEquals(
                "Pago no encontrado",
                exception.getMessage());

        verify(
                pagoRepository,
                never())
                .findById(
                        99);
    }

    @Test
    void administradorPagoInexistenteResponde404() {

        when(
                sedeAccessService.esAdministrador(
                        authentication)
        ).thenReturn(
                true);

        when(
                pagoRepository.findById(
                        99)
        ).thenReturn(
                Optional.empty());

        RecursoNoEncontradoException exception =
                assertThrows(
                        RecursoNoEncontradoException.class,
                        () ->
                                service.obtenerAccesible(
                                        99,
                                        authentication));

        assertEquals(
                "Pago no encontrado",
                exception.getMessage());
    }

    @Test
    void idPagoInvalidoEsRechazadoAntesDeConsultarRoles() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                service.obtenerAccesible(
                                        0,
                                        authentication));

        assertEquals(
                "El id de pago debe ser positivo",
                exception.getMessage());

        verifyNoInteractions(
                pagoRepository,
                sedeAccessService);
    }

    @Test
    void rolNoBackofficeEsRechazado() {

        when(
                sedeAccessService.esAdministrador(
                        authentication)
        ).thenReturn(
                false);

        when(
                sedeAccessService.esEncargadoLocal(
                        authentication)
        ).thenReturn(
                false);

        assertThrows(
                AccessDeniedException.class,
                () ->
                        service.obtenerAccesible(
                                30,
                                authentication));

        verifyNoInteractions(
                pagoRepository);
    }

    private Pago crearPago(
            Integer idPago,
            Integer idReserva) {

        Reserva reserva =
                new Reserva();

        reserva.setIdReserva(
                idReserva);

        Pago pago =
                new Pago();

        pago.setIdPago(
                idPago);

        pago.setReserva(
                reserva);

        pago.setMonto(
                new BigDecimal("100.00"));

        pago.setMetodoPago(
                "PRUEBA");

        pago.setEstadoPago(
                "PENDIENTE");

        pago.setFechaCreacion(
                LocalDateTime.of(
                        2026,
                        10,
                        17,
                        3,
                        0));

        return pago;
    }
}