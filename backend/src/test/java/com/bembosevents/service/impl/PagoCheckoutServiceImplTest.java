package com.bembosevents.service.impl;

import com.bembosevents.dto.pago.CrearPagoRequest;
import com.bembosevents.dto.pago.IniciarPagoCheckoutRequest;
import com.bembosevents.dto.pago.PagoCheckoutResponse;
import com.bembosevents.dto.pago.PagoResponse;
import com.bembosevents.integration.mercadopago.MercadoPagoOrderRequest;
import com.bembosevents.integration.mercadopago.MercadoPagoOrderResult;
import com.bembosevents.integration.mercadopago.MercadoPagoProvider;
import com.bembosevents.service.PagoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PagoCheckoutServiceImplTest {

    @Mock
    private PagoService pagoService;

    @Mock
    private MercadoPagoProvider mercadoPagoProvider;

    @Test
    void debeCrearPagoLocalYOrderMercadoPago() {

        PagoResponse pago =
                new PagoResponse(
                        25,
                        10,
                        new BigDecimal("150.00"),
                        "MERCADO_PAGO",
                        "PENDIENTE",
                        null,
                        null,
                        LocalDateTime.now());

        PagoResponse vinculado =
                new PagoResponse(
                        25,
                        10,
                        new BigDecimal("150.00"),
                        "MERCADO_PAGO",
                        "PENDIENTE",
                        "ORD-123",
                        null,
                        pago.fechaCreacion());

        when(pagoService.iniciarPago(
                "cliente@testuser.com",
                10,
                new CrearPagoRequest(
                        new BigDecimal("150.00"),
                        "MERCADO_PAGO")))
                .thenReturn(pago);

        when(mercadoPagoProvider.crearOrden(
                org.mockito.ArgumentMatchers.any(),
                anyString()))
                .thenReturn(
                        new MercadoPagoOrderResult(
                                "ORD-123",
                                "created",
                                "https://mercadopago.test/checkout/ORD-123",
                                "PAGO-25"));

        when(pagoService.vincularReferenciaExterna(
                25,
                "ORD-123"))
                .thenReturn(vinculado);

        PagoCheckoutServiceImpl service =
                new PagoCheckoutServiceImpl(
                        pagoService,
                        mercadoPagoProvider);

        PagoCheckoutResponse result =
                service.iniciarCheckout(
                        "cliente@testuser.com",
                        10,
                        new IniciarPagoCheckoutRequest(
                                new BigDecimal("150.00")));

        assertEquals(
                "ORD-123",
                result.orderId());

        assertEquals(
                "created",
                result.orderStatus());

        assertEquals(
                "https://mercadopago.test/checkout/ORD-123",
                result.checkoutUrl());

        ArgumentCaptor<MercadoPagoOrderRequest> captor =
                ArgumentCaptor.forClass(
                        MercadoPagoOrderRequest.class);

        verify(mercadoPagoProvider)
                .crearOrden(
                        captor.capture(),
                        anyString());

        assertEquals(
                "PAGO-25",
                captor.getValue().externalReference());

        assertEquals(
                "cliente@testuser.com",
                captor.getValue().payerEmail());

        verify(pagoService)
                .vincularReferenciaExterna(
                        25,
                        "ORD-123");
    }

    @Test
    void debeFallarSiProviderNoDevuelveCheckoutUrl() {

        PagoResponse pago =
                new PagoResponse(
                        25,
                        10,
                        new BigDecimal("150.00"),
                        "MERCADO_PAGO",
                        "PENDIENTE",
                        null,
                        null,
                        LocalDateTime.now());

        when(pagoService.iniciarPago(
                "cliente@testuser.com",
                10,
                new CrearPagoRequest(
                        new BigDecimal("150.00"),
                        "MERCADO_PAGO")))
                .thenReturn(pago);

        when(mercadoPagoProvider.crearOrden(
                org.mockito.ArgumentMatchers.any(),
                anyString()))
                .thenReturn(
                        new MercadoPagoOrderResult(
                                "ORD-123",
                                "created",
                                null,
                                "PAGO-25"));

        PagoCheckoutServiceImpl service =
                new PagoCheckoutServiceImpl(
                        pagoService,
                        mercadoPagoProvider);

        assertThrows(
                IllegalStateException.class,
                () -> service.iniciarCheckout(
                        "cliente@testuser.com",
                        10,
                        new IniciarPagoCheckoutRequest(
                                new BigDecimal("150.00"))));
    }
}