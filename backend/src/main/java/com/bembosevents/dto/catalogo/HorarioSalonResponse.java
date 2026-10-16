package com.bembosevents.dto.catalogo;

import java.time.LocalTime;

public record HorarioSalonResponse(
        Integer idHorarioSalon,
        Integer idSalon,
        String diaSemana,
        LocalTime horaInicio,
        LocalTime horaFin) {
}
