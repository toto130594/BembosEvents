package com.bembosevents.dto.pago;

import com.bembosevents.domain.EstadoPago;
import jakarta.validation.constraints.NotNull;

public record TransicionPagoRequest(

        @NotNull(message = "El estado actual es obligatorio")
        EstadoPago estadoActual,

        @NotNull(message = "El nuevo estado es obligatorio")
        EstadoPago nuevoEstado) {
}