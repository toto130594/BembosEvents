package com.bembosevents.dto.reserva;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public record ReservaDetalleResponse(
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
        BigDecimal montoBase,
        BigDecimal montoServicios,
        BigDecimal montoTotal,
        BigDecimal porcentajeDeposito,
        BigDecimal montoDeposito,
        List<ReservaDetalleServicioResponse> serviciosAdicionales,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion) {
}