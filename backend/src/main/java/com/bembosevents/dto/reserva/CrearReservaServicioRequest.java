package com.bembosevents.dto.reserva;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CrearReservaServicioRequest(

        @NotNull(message = "El id del servicio adicional es obligatorio")
        @Positive(message = "El id del servicio adicional debe ser positivo")
        Integer idServicioAdicional,

        @NotNull(message = "La cantidad del servicio es obligatoria")
        @Positive(message = "La cantidad del servicio debe ser mayor que cero")
        Integer cantidad) {
}
