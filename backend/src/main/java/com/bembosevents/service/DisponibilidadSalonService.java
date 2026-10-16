package com.bembosevents.service;

import com.bembosevents.dto.catalogo.DisponibilidadSalonResponse;

import java.time.LocalDate;
import java.time.LocalTime;

public interface DisponibilidadSalonService {

    DisponibilidadSalonResponse consultar(
            Integer idSalon,
            LocalDate fecha,
            LocalTime horaInicio,
            LocalTime horaFin);
}
