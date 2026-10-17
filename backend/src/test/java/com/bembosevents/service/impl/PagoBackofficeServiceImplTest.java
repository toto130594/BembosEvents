package com.bembosevents.service.impl;

import com.bembosevents.dto.pago.PagoResponse;
import com.bembosevents.entity.Pago;
import com.bembosevents.entity.Reserva;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PagoBackofficeServiceImplTest {

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
    void administradorListaTodosLosPagos() {

        Pago pago1 =
                crearPago(
                        20,
                        101,
                        "200.00");

        Pago pago2 =
                crearPago(
                        19,
                        100,
                        "100.00");

        when(
                sedeAccessService.esAdministrador(
                        authentication)
        ).thenReturn(
                true);

        when(
                pagoRepository
                        .findAllByOrderByFechaCreacionDesc()
        ).thenReturn(
                List.of(
                        pago1,
                        pago2));

        List<PagoResponse> response =
                service.listarAccesibles(
                        authentication);

        assertEquals(
                2,
                response.size());

        assertEquals(
                20,
                response.get(0).idPago());

        assertEquals(
                19,
                response.get(1).idPago());

        verify(
                pagoRepository)
                .findAllByOrderByFechaCreacionDesc();

        verify(
                pagoRepository,
                never())
                .findByReserva_Sede_IdSedeOrderByFechaCreacionDesc(
                        org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    void encargadoListaSoloPagosDeSuSede() {

        Pago pago =
                crearPago(
                        30,
                        200,
                        "300.00");

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
                        .findByReserva_Sede_IdSedeOrderByFechaCreacionDesc(
                                5)
        ).thenReturn(
                List.of(pago));

        List<PagoResponse> response =
                service.listarAccesibles(
                        authentication);

        assertEquals(
                1,
                response.size());

        assertEquals(
                30,
                response.get(0).idPago());

        verify(
                pagoRepository)
                .findByReserva_Sede_IdSedeOrderByFechaCreacionDesc(
                        5);

        verify(
                pagoRepository,
                never())
                .findAllByOrderByFechaCreacionDesc();
    }

    @Test
    void encargadoSinPagosRetornaListaVacia() {

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
                        .findByReserva_Sede_IdSedeOrderByFechaCreacionDesc(
                                5)
        ).thenReturn(
                List.of());

        List<PagoResponse> response =
                service.listarAccesibles(
                        authentication);

        assertEquals(
                0,
                response.size());
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
                        service.listarAccesibles(
                                authentication));

        verify(
                pagoRepository,
                never())
                .findAllByOrderByFechaCreacionDesc();
    }

    private Pago crearPago(
            Integer idPago,
            Integer idReserva,
            String monto) {

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
                new BigDecimal(monto));

        pago.setMetodoPago(
                "PRUEBA");

        pago.setEstadoPago(
                "PENDIENTE");

        pago.setFechaCreacion(
                LocalDateTime.of(
                        2026,
                        10,
                        17,
                        2,
                        idPago % 60));

        return pago;
    }
}