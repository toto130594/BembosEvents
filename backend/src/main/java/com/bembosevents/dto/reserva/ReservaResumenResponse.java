package com.bembosevents.dto.reserva;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record ReservaResumenResponse(
        Integer idReserva,
        String codigoReserva,
        String estado,
        Integer idSede,
        Integer idSalon,
        Integer idPaquete,
        Integer idTematica,
        LocalDate fechaEvento,
        LocalTime horaInicio,
        LocalTime horaFin,
        Integer cantidadInvitados,
        BigDecimal montoTotal,
        BigDecimal montoDeposito,
        LocalDateTime fechaCreacion) {
}