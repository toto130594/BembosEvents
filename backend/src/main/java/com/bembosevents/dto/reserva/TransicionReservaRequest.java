package com.bembosevents.dto.reserva;

import com.bembosevents.domain.EstadoReserva;
import jakarta.validation.constraints.NotNull;

public record TransicionReservaRequest(

        @NotNull(message = "El estado actual es obligatorio")
        EstadoReserva estadoActual,

        @NotNull(message = "El nuevo estado es obligatorio")
        EstadoReserva nuevoEstado) {
}