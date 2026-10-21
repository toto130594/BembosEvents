package com.bembosevents.integration.mercadopago;

public record MercadoPagoOrderResult(
        String id,
        String status,
        String statusDetail,
        String checkoutUrl,
        String externalReference) {

    public MercadoPagoOrderResult(
            String id,
            String status,
            String checkoutUrl,
            String externalReference) {

        this(
                id,
                status,
                null,
                checkoutUrl,
                externalReference);
    }
}