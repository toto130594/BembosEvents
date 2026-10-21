package com.bembosevents.dto.pago;

public record PagoCheckoutResponse(
        PagoResponse pago,
        String proveedor,
        String orderId,
        String orderStatus,
        String checkoutUrl) {
}