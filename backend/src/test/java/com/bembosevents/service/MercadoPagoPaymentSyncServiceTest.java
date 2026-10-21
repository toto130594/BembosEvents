package com.bembosevents.service;

import com.bembosevents.domain.EstadoPago;
import com.bembosevents.dto.pago.PagoResponse;
import com.bembosevents.integration.mercadopago.MercadoPagoOrderResult;
import com.bembosevents.integration.mercadopago.MercadoPagoOrderStatusMapper;
import com.bembosevents.integration.mercadopago.MercadoPagoProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MercadoPagoPaymentSyncServiceTest {

    @Mock
    private MercadoPagoProvider provider;

    @Mock
    private PagoService pagoService;

    private final MercadoPagoOrderStatusMapper mapper =
            new MercadoPagoOrderStatusMapper();

    @Test
    void debeAprobarSoloLuegoDeConsultarOrder() {

        when(provider.consultarOrden(
                "ORD-1107"))
                .thenReturn(
                        new MercadoPagoOrderResult(
                                "ORD-1107",
                                "processed",
                                "accredited",
                                null,
                                "PAGO-25"));

        when(pagoService.actualizarEstadoDesdeProveedor(
                "ORD-1107",
                EstadoPago.APROBADO))
                .thenReturn(
                        pago(
                                "APROBADO"));

        MercadoPagoPaymentSyncService service =
                new MercadoPagoPaymentSyncService(
                        provider,
                        mapper,
                        pagoService);

        String result =
                service.sincronizar(
                        "ORD-1107");

        assertEquals(
                "ESTADO_APROBADO",
                result);

        verify(provider)
                .consultarOrden(
                        "ORD-1107");

        verify(pagoService)
                .actualizarEstadoDesdeProveedor(
                        "ORD-1107",
                        EstadoPago.APROBADO);
    }

    @Test
    void estadoNoFinalNoDebeMutarPago() {

        when(provider.consultarOrden(
                "ORD-1107"))
                .thenReturn(
                        new MercadoPagoOrderResult(
                                "ORD-1107",
                                "processing",
                                "in_process",
                                null,
                                "PAGO-25"));

        MercadoPagoPaymentSyncService service =
                new MercadoPagoPaymentSyncService(
                        provider,
                        mapper,
                        pagoService);

        String result =
                service.sincronizar(
                        "ORD-1107");

        assertEquals(
                "SIN_CAMBIO",
                result);

        verify(pagoService, never())
                .actualizarEstadoDesdeProveedor(
                        org.mockito.ArgumentMatchers.anyString(),
                        org.mockito.ArgumentMatchers.any());
    }

    private PagoResponse pago(
            String estado) {

        return new PagoResponse(
                25,
                10,
                new BigDecimal("150.00"),
                "MERCADO_PAGO",
                estado,
                "ORD-1107",
                LocalDateTime.now(),
                LocalDateTime.now());
    }
}