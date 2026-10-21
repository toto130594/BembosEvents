package com.bembosevents.service;

import com.bembosevents.domain.EstadoPago;
import com.bembosevents.dto.pago.PagoRefundResponse;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MercadoPagoRefundServiceTest {

    @Mock
    private PagoRepository pagoRepository;

    @Mock
    private MercadoPagoProvider provider;

    @Mock
    private PagoService pagoService;

    private final MercadoPagoOrderStatusMapper mapper =
            new MercadoPagoOrderStatusMapper();

    @Test
    void debeReembolsarPagoAprobado() {

        Pago pago =
                pago(
                        "APROBADO");

        when(pagoRepository.findById(
                25))
                .thenReturn(
                        Optional.of(
                                pago));

        when(provider.consultarOrden(
                "ORD-1108"))
                .thenReturn(
                        order(
                                "processed",
                                "accredited"));

        when(provider.reembolsarOrdenTotal(
                org.mockito.ArgumentMatchers.eq(
                        "ORD-1108"),
                anyString()))
                .thenReturn(
                        order(
                                "refunded",
                                "refunded"));

        when(pagoService.actualizarEstadoDesdeProveedor(
                "ORD-1108",
                EstadoPago.REEMBOLSADO))
                .thenReturn(
                        response(
                                "REEMBOLSADO"));

        MercadoPagoRefundService service =
                service();

        PagoRefundResponse result =
                service.reembolsarTotal(
                        25);

        assertEquals(
                "REEMBOLSADO",
                result.pago()
                        .estadoPago());

        assertTrue(
                result.providerCallExecuted());

        verify(provider)
                .reembolsarOrdenTotal(
                        org.mockito.ArgumentMatchers.eq(
                                "ORD-1108"),
                        anyString());
    }

    @Test
    void pagoYaReembolsadoDebeSerIdempotenteLocal() {

        when(pagoRepository.findById(
                25))
                .thenReturn(
                        Optional.of(
                                pago(
                                        "REEMBOLSADO")));

        PagoRefundResponse result =
                service()
                        .reembolsarTotal(
                                25);

        assertFalse(
                result.providerCallExecuted());

        verify(provider, never())
                .consultarOrden(
                        anyString());

        verify(provider, never())
                .reembolsarOrdenTotal(
                        anyString(),
                        anyString());
    }

    @Test
    void providerYaReembolsadoDebeSincronizarLocalSinNuevoRefund() {

        when(pagoRepository.findById(
                25))
                .thenReturn(
                        Optional.of(
                                pago(
                                        "APROBADO")));

        when(provider.consultarOrden(
                "ORD-1108"))
                .thenReturn(
                        order(
                                "refunded",
                                "refunded"));

        when(pagoService.actualizarEstadoDesdeProveedor(
                "ORD-1108",
                EstadoPago.REEMBOLSADO))
                .thenReturn(
                        response(
                                "REEMBOLSADO"));

        PagoRefundResponse result =
                service()
                        .reembolsarTotal(
                                25);

        assertFalse(
                result.providerCallExecuted());

        verify(provider, never())
                .reembolsarOrdenTotal(
                        anyString(),
                        anyString());
    }

    @Test
    void pagoPendienteNoDebeLlamarProveedor() {

        when(pagoRepository.findById(
                25))
                .thenReturn(
                        Optional.of(
                                pago(
                                        "PENDIENTE")));

        assertThrows(
                IllegalStateException.class,
                () -> service()
                        .reembolsarTotal(
                                25));

        verify(provider, never())
                .consultarOrden(
                        anyString());
    }

    private MercadoPagoRefundService service() {

        return new MercadoPagoRefundService(
                pagoRepository,
                provider,
                mapper,
                pagoService);
    }

    private Pago pago(
            String estado) {

        Reserva reserva =
                new Reserva();

        reserva.setIdReserva(
                10);

        Pago pago =
                new Pago();

        pago.setIdPago(
                25);

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
                "ORD-1108");

        pago.setFechaPago(
                LocalDateTime.now());

        pago.setFechaCreacion(
                LocalDateTime.now());

        return pago;
    }

    private MercadoPagoOrderResult order(
            String status,
            String detail) {

        return new MercadoPagoOrderResult(
                "ORD-1108",
                status,
                detail,
                null,
                "PAGO-25");
    }

    private PagoResponse response(
            String estado) {

        return new PagoResponse(
                25,
                10,
                new BigDecimal(
                        "150.00"),
                "MERCADO_PAGO",
                estado,
                "ORD-1108",
                LocalDateTime.now(),
                LocalDateTime.now());
    }
}