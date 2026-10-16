package com.bembosevents.dto.reserva;

import java.math.BigDecimal;

public record ReservaDetalleServicioResponse(
        Integer idServicioAdicional,
        Integer cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal) {
}