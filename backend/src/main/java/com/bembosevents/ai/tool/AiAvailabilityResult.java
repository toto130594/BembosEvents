package com.bembosevents.ai.tool;

import java.time.LocalDate;
import java.time.LocalTime;

public record AiAvailabilityResult(
        Integer idConversacion,
        Integer idSede,
        Integer idSalon,
        String nombreSalon,
        Integer capacidad,
        LocalDate fecha,
        LocalTime horaInicio,
        LocalTime horaFin,
        boolean disponible,
        String motivo) {
}