package com.bembosevents.dto.pago;

public record PagoReconciliationItemResponse(
        Integer idPago,
        String orderId,
        String estadoLocalAnterior,
        String providerStatus,
        String providerStatusDetail,
        String estadoObjetivo,
        String resultado,
        String mensaje) {
}