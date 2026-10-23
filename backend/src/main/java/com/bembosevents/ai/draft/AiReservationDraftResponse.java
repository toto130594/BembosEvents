package com.bembosevents.ai.draft;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public record AiReservationDraftResponse(
        Integer idBorrador,
        Integer idConversacion,
        Integer idCliente,
        Integer idSede,
        Integer idSalon,
        Integer idPaquete,
        Integer idTematica,
        LocalDate fechaEvento,
        LocalTime horaInicio,
        LocalTime horaFin,
        Integer cantidadInvitados,
        List<AiDraftServiceSelection> serviciosAdicionales,
        String estado,
        boolean listoParaConfirmar,
        List<String> camposFaltantes,
        Integer idReservaConfirmada,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion,
        LocalDateTime fechaConfirmacion) {
}