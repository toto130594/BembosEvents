package com.bembosevents.service;

import com.bembosevents.domain.EstadoPago;
import com.bembosevents.dto.pago.PagoReconciliationItemResponse;
import com.bembosevents.dto.pago.PagoReconciliationResponse;
import com.bembosevents.dto.pago.PagoResponse;
import com.bembosevents.entity.Pago;
import com.bembosevents.entity.Reserva;
import com.bembosevents.integration.mercadopago.MercadoPagoOrderResult;
import com.bembosevents.integration.mercadopago.MercadoPagoOrderStatusMapper;
import com.bembosevents.integration.mercadopago.MercadoPagoProvider;
import com.bembosevents.repository.PagoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MercadoPagoReconciliationServiceTest {

    @Mock
    private PagoRepository pagoRepository;

    @Mock
    private MercadoPagoProvider provider;

    @Mock
    private PagoService pagoService;

    private final MercadoPagoOrderStatusMapper mapper =
            new MercadoPagoOrderStatusMapper();

    @Test
    void pendienteAcreditadoDebeActualizarse() {

        Pago pago =
                pago(
                        25,
                        "PENDIENTE",
                        "ORD-1109");

        when(pagoRepository.findById(
                25))
                .thenReturn(
                        Optional.of(
                                pago));

        when(provider.consultarOrden(
                "ORD-1109"))
                .thenReturn(
                        order(
                                "ORD-1109",
                                "processed",
                                "accredited"));

        when(pagoService.actualizarEstadoDesdeProveedor(
                "ORD-1109",
                EstadoPago.APROBADO))
                .thenReturn(
                        response(
                                25,
                                "APROBADO",
                                "ORD-1109"));

        MercadoPagoReconciliationService service =
                service();

        PagoReconciliationItemResponse result =
                service.reconciliarPago(
                        25);

        assertEquals(
                "ACTUALIZADO",
                result.resultado());

        assertEquals(
                "APROBADO",
                result.estadoObjetivo());

        verify(pagoService)
                .actualizarEstadoDesdeProveedor(
                        "ORD-1109",
                        EstadoPago.APROBADO);
    }

    @Test
    void aprobadoAcreditadoDebeEstarEnSincronia() {

        Pago pago =
                pago(
                        25,
                        "APROBADO",
                        "ORD-1109");

        when(pagoRepository.findById(
                25))
                .thenReturn(
                        Optional.of(
                                pago));

        when(provider.consultarOrden(
                "ORD-1109"))
                .thenReturn(
                        order(
                                "ORD-1109",
                                "processed",
                                "accredited"));

        PagoReconciliationItemResponse result =
                service()
                        .reconciliarPago(
                                25);

        assertEquals(
                "EN_SINCRONIA",
                result.resultado());

        verify(pagoService, never())
                .actualizarEstadoDesdeProveedor(
                        org.mockito.ArgumentMatchers.anyString(),
                        org.mockito.ArgumentMatchers.any());
    }

    @Test
    void createdDebeQuedarSinCambio() {

        Pago pago =
                pago(
                        25,
                        "PENDIENTE",
                        "ORD-1109");

        when(pagoRepository.findById(
                25))
                .thenReturn(
                        Optional.of(
                                pago));

        when(provider.consultarOrden(
                "ORD-1109"))
                .thenReturn(
                        order(
                                "ORD-1109",
                                "created",
                                "created"));

        PagoReconciliationItemResponse result =
                service()
                        .reconciliarPago(
                                25);

        assertEquals(
                "SIN_CAMBIO",
                result.resultado());
    }

    @Test
    void terminalLocalNoDebeForzarTransicionInvalida() {

        Pago pago =
                pago(
                        25,
                        "RECHAZADO",
                        "ORD-1109");

        when(pagoRepository.findById(
                25))
                .thenReturn(
                        Optional.of(
                                pago));

        when(provider.consultarOrden(
                "ORD-1109"))
                .thenReturn(
                        order(
                                "ORD-1109",
                                "processed",
                                "accredited"));

        PagoReconciliationItemResponse result =
                service()
                        .reconciliarPago(
                                25);

        assertEquals(
                "CONFLICTO",
                result.resultado());

        verify(pagoService, never())
                .actualizarEstadoDesdeProveedor(
                        org.mockito.ArgumentMatchers.anyString(),
                        org.mockito.ArgumentMatchers.any());
    }

    @Test
    void loteDebeContinuarCuandoUnaOrderFalla() {

        Pago first =
                pago(
                        25,
                        "PENDIENTE",
                        "ORD-OK");

        Pago second =
                pago(
                        26,
                        "PENDIENTE",
                        "ORD-ERROR");

        when(pagoRepository.findAllByOrderByFechaCreacionDesc())
                .thenReturn(
                        List.of(
                                first,
                                second));

        when(provider.consultarOrden(
                "ORD-OK"))
                .thenReturn(
                        order(
                                "ORD-OK",
                                "created",
                                "created"));

        when(provider.consultarOrden(
                "ORD-ERROR"))
                .thenThrow(
                        new IllegalStateException(
                                "provider unavailable"));

        PagoReconciliationResponse result =
                service()
                        .reconciliarLote();

        assertEquals(
                2,
                result.total());

        assertEquals(
                1,
                result.sinCambio());

        assertEquals(
                1,
                result.errores());
    }

    private MercadoPagoReconciliationService service() {

        return new MercadoPagoReconciliationService(
                pagoRepository,
                provider,
                mapper,
                pagoService);
    }

    private Pago pago(
            int id,
            String estado,
            String orderId) {

        Reserva reserva =
                new Reserva();

        reserva.setIdReserva(
                10);

        Pago pago =
                new Pago();

        pago.setIdPago(
                id);

        pago.setReserva(
                reserva);

        pago.setMonto(
                new BigDecimal(
                        "150.00"));

        pago.setMetodoPago(
                "MERCADO_PAGO");

        pago.setEstadoPago(
                estado);

        pago.setReferenciaExterna(
                orderId);

        pago.setFechaCreacion(
                LocalDateTime.now());

        return pago;
    }

    private MercadoPagoOrderResult order(
            String id,
            String status,
            String detail) {

        return new MercadoPagoOrderResult(
                id,
                status,
                detail,
                null,
                "PAGO-25");
    }

    private PagoResponse response(
            int id,
            String estado,
            String orderId) {

        return new PagoResponse(
                id,
                10,
                new BigDecimal(
                        "150.00"),
                "MERCADO_PAGO",
                estado,
                orderId,
                LocalDateTime.now(),
                LocalDateTime.now());
    }
}