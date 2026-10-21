package com.bembosevents.integration.mercadopago;

public interface MercadoPagoProvider {

    MercadoPagoOrderResult crearOrden(
            MercadoPagoOrderRequest request,
            String idempotencyKey);

    MercadoPagoOrderResult consultarOrden(
            String orderId);
    MercadoPagoOrderResult reembolsarOrdenTotal(
            String orderId,
            String idempotencyKey);
}