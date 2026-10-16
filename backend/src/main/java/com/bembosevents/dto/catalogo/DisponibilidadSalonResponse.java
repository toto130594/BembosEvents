package com.bembosevents.dto.catalogo;

import java.time.LocalDate;
import java.time.LocalTime;

public record DisponibilidadSalonResponse(
        Integer idSalon,
        LocalDate fecha,
        LocalTime horaInicio,
        LocalTime horaFin,
        boolean disponible,
        String motivo) {
}
