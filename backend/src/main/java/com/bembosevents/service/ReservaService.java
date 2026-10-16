package com.bembosevents.service;

import com.bembosevents.domain.EstadoReserva;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public interface ReservaService {

    void validarCapacidad(
            Integer idSalon,
            Integer cantidadInvitados);

    void validarDisponibilidad(
            Integer idSalon,
            LocalDate fechaEvento,
            LocalTime horaInicio,
            LocalTime horaFin);

    void validarMontoMinimo(
            BigDecimal montoTotal);

    BigDecimal calcularMontoTotal(
            BigDecimal montoBase,
            BigDecimal montoServicios);

    BigDecimal calcularMontoDeposito(
            BigDecimal montoTotal);

    void validarTransicionEstado(
            EstadoReserva estadoActual,
            EstadoReserva nuevoEstado);
}