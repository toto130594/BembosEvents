package com.bembosevents.service.impl;

import com.bembosevents.dto.pago.CrearPagoRequest;
import com.bembosevents.dto.pago.IniciarPagoCheckoutRequest;
import com.bembosevents.dto.pago.PagoCheckoutResponse;
import com.bembosevents.dto.pago.PagoResponse;
import com.bembosevents.integration.mercadopago.MercadoPagoOrderRequest;
import com.bembosevents.integration.mercadopago.MercadoPagoOrderResult;
import com.bembosevents.integration.mercadopago.MercadoPagoProvider;
import com.bembosevents.service.PagoCheckoutService;
import com.bembosevents.service.PagoService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PagoCheckoutServiceImpl
        implements PagoCheckoutService {

    private static final String METODO_MERCADO_PAGO =
            "MERCADO_PAGO";

    private final PagoService pagoService;
    private final MercadoPagoProvider mercadoPagoProvider;

    public PagoCheckoutServiceImpl(
            PagoService pagoService,
            MercadoPagoProvider mercadoPagoProvider) {

        this.pagoService = pagoService;
        this.mercadoPagoProvider = mercadoPagoProvider;
    }

    @Override
    public PagoCheckoutResponse iniciarCheckout(
            String correoCliente,
            Integer idReserva,
            IniciarPagoCheckoutRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "La solicitud de checkout es obligatoria");
        }

        PagoResponse pago =
                pagoService.iniciarPago(
                        correoCliente,
                        idReserva,
                        new CrearPagoRequest(
                                request.monto(),
                                METODO_MERCADO_PAGO));

        String externalReference =
                "PAGO-" + pago.idPago();

        String idempotencyKey =
                UUID.randomUUID().toString();

        MercadoPagoOrderResult order =
                mercadoPagoProvider.crearOrden(
                        new MercadoPagoOrderRequest(
                                pago.monto(),
                                externalReference,
                                correoCliente,
                                "Reserva Bembos Events #"
                                        + idReserva),
                        idempotencyKey);

        validarOrder(order);

        PagoResponse pagoVinculado =
                pagoService.vincularReferenciaExterna(
                        pago.idPago(),
                        order.id());

        return new PagoCheckoutResponse(
                pagoVinculado,
                "MERCADO_PAGO",
                order.id(),
                order.status(),
                order.checkoutUrl());
    }

    private void validarOrder(
            MercadoPagoOrderResult order) {

        if (order == null) {
            throw new IllegalStateException(
                    "Mercado Pago devolvio una order vacia");
        }

        if (order.id() == null
                || order.id().isBlank()) {

            throw new IllegalStateException(
                    "Mercado Pago no devolvio orderId");
        }

        if (order.checkoutUrl() == null
                || order.checkoutUrl().isBlank()) {

            throw new IllegalStateException(
                    "Mercado Pago no devolvio checkoutUrl");
        }
    }
}