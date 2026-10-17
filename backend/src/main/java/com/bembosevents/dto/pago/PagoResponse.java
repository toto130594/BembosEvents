package com.bembosevents.dto.pago;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PagoResponse(

        Integer idPago,
        Integer idReserva,
        BigDecimal monto,
        String metodoPago,
        String estadoPago,
        String referenciaExterna,
        LocalDateTime fechaPago,
        LocalDateTime fechaCreacion) {
}