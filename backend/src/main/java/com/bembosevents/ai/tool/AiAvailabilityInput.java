package com.bembosevents.ai.tool;

import java.time.LocalDate;
import java.time.LocalTime;

public record AiAvailabilityInput(
        Integer idSalon,
        LocalDate fecha,
        LocalTime horaInicio,
        LocalTime horaFin) {

    public AiAvailabilityInput {
        if (idSalon == null
                || idSalon <= 0) {
            throw new IllegalArgumentException(
                    "El salon es obligatorio");
        }

        if (fecha == null
                || horaInicio == null
                || horaFin == null) {
            throw new IllegalArgumentException(
                    "Fecha y horario son obligatorios");
        }

        if (!horaInicio.isBefore(horaFin)) {
            throw new IllegalArgumentException(
                    "La hora de inicio debe ser anterior a la hora de fin");
        }
    }
}