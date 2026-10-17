package com.bembosevents.dto.reserva;

import com.bembosevents.domain.EstadoReserva;
import jakarta.validation.constraints.NotNull;

public record CambiarEstadoReservaRequest(

        @NotNull(message = "El nuevo estado es obligatorio")
        EstadoReserva nuevoEstado
) {
}