package com.bembosevents.dto.pago;

import java.util.List;

public record PagoReconciliationResponse(
        int total,
        int actualizados,
        int enSincronia,
        int sinCambio,
        int conflictos,
        int errores,
        List<PagoReconciliationItemResponse> items) {
}