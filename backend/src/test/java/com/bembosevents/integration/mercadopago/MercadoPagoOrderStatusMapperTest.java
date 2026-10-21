package com.bembosevents.integration.mercadopago;

import com.bembosevents.domain.EstadoPago;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MercadoPagoOrderStatusMapperTest {

    private final MercadoPagoOrderStatusMapper mapper =
            new MercadoPagoOrderStatusMapper();

    @Test
    void processedAccreditedDebeSerAprobado() {

        assertEquals(
                EstadoPago.APROBADO,
                mapper.map(
                        order(
                                "processed",
                                "accredited"))
                        .orElseThrow());
    }

    @Test
    void failedDebeSerRechazado() {

        assertEquals(
                EstadoPago.RECHAZADO,
                mapper.map(
                        order(
                                "failed",
                                "cc_rejected_other_reason"))
                        .orElseThrow());
    }

    @Test
    void canceledDebeSerCancelado() {

        assertEquals(
                EstadoPago.CANCELADO,
                mapper.map(
                        order(
                                "canceled",
                                "canceled"))
                        .orElseThrow());
    }

    @Test
    void processedRefundedDebeSerReembolsado() {

        assertEquals(
                EstadoPago.REEMBOLSADO,
                mapper.map(
                        order(
                                "processed",
                                "refunded"))
                        .orElseThrow());
    }

    @Test
    void createdNoDebeCambiarEstadoInterno() {

        assertTrue(
                mapper.map(
                        order(
                                "created",
                                "created"))
                        .isEmpty());
    }

    @Test
    void processingNoDebeCambiarEstadoInterno() {

        assertTrue(
                mapper.map(
                        order(
                                "processing",
                                "in_process"))
                        .isEmpty());
    }

    @Test
    void partiallyRefundedNoDebeForzarEnumInexistente() {

        assertTrue(
                mapper.map(
                        order(
                                "processed",
                                "partially_refunded"))
                        .isEmpty());
    }

    private MercadoPagoOrderResult order(
            String status,
            String detail) {

        return new MercadoPagoOrderResult(
                "ORD-1107",
                status,
                detail,
                null,
                "PAGO-25");
    }
}