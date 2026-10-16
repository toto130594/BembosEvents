package com.bembosevents.dto.reserva;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record CrearReservaRequest(

        @NotNull(message = "El id de sede es obligatorio")
        @Positive(message = "El id de sede debe ser positivo")
        Integer idSede,

        @NotNull(message = "El id de salon es obligatorio")
        @Positive(message = "El id de salon debe ser positivo")
        Integer idSalon,

        @Positive(message = "El id de paquete debe ser positivo")
        Integer idPaquete,

        @Positive(message = "El id de tematica debe ser positivo")
        Integer idTematica,

        @NotNull(message = "La fecha del evento es obligatoria")
        LocalDate fechaEvento,

        @NotNull(message = "La hora de inicio es obligatoria")
        LocalTime horaInicio,

        @NotNull(message = "La hora de fin es obligatoria")
        LocalTime horaFin,

        @NotNull(message = "La cantidad de invitados es obligatoria")
        @Positive(message = "La cantidad de invitados debe ser mayor que cero")
        Integer cantidadInvitados,

        @Valid
        List<CrearReservaServicioRequest> serviciosAdicionales) {
}
