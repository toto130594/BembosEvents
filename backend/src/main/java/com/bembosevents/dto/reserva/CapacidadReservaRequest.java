package com.bembosevents.dto.reserva;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CapacidadReservaRequest(

        @NotNull(message = "El id del salon es obligatorio")
        @Positive(message = "El id del salon debe ser positivo")
        Integer idSalon,

        @NotNull(message = "La cantidad de invitados es obligatoria")
        @Positive(message = "La cantidad de invitados debe ser mayor que cero")
        Integer cantidadInvitados) {
}