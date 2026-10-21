package com.bembosevents.dto.pago;

public record PagoRefundResponse(
        PagoResponse pago,
        String proveedor,
        String orderId,
        String providerStatus,
        String providerStatusDetail,
        boolean providerCallExecuted) {
}