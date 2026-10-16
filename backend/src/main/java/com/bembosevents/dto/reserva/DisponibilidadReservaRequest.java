package com.bembosevents.dto.reserva;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.time.LocalTime;

public record DisponibilidadReservaRequest(

        @NotNull(message = "El id del salon es obligatorio")
        @Positive(message = "El id del salon debe ser positivo")
        Integer idSalon,

        @NotNull(message = "La fecha del evento es obligatoria")
        LocalDate fechaEvento,

        @NotNull(message = "La hora de inicio es obligatoria")
        LocalTime horaInicio,

        @NotNull(message = "La hora de fin es obligatoria")
        LocalTime horaFin) {
}