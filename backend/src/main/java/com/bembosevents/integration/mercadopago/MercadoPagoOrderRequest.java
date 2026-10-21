package com.bembosevents.integration.mercadopago;

import java.math.BigDecimal;

public record MercadoPagoOrderRequest(
        BigDecimal totalAmount,
        String externalReference,
        String payerEmail,
        String title) {
}