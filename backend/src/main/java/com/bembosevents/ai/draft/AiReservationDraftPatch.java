package com.bembosevents.ai.draft;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record AiReservationDraftPatch(
        Integer idSede,
        Integer idSalon,
        Integer idPaquete,
        Integer idTematica,
        LocalDate fechaEvento,
        LocalTime horaInicio,
        LocalTime horaFin,
        Integer cantidadInvitados,
        List<AiDraftServiceSelection> serviciosAdicionales,
        boolean limpiarPaquete,
        boolean limpiarTematica,
        boolean limpiarServicios) {

    public AiReservationDraftPatch {
        if (idSede != null
                && idSede <= 0) {
            throw new IllegalArgumentException(
                    "La sede debe ser valida");
        }

        if (idSalon != null
                && idSalon <= 0) {
            throw new IllegalArgumentException(
                    "El salon debe ser valido");
        }

        if (idPaquete != null
                && idPaquete <= 0) {
            throw new IllegalArgumentException(
                    "El paquete debe ser valido");
        }

        if (idTematica != null
                && idTematica <= 0) {
            throw new IllegalArgumentException(
                    "La tematica debe ser valida");
        }

        if (cantidadInvitados != null
                && cantidadInvitados <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad de invitados debe ser mayor que cero");
        }

        if (horaInicio != null
                && horaFin != null
                && !horaInicio.isBefore(horaFin)) {
            throw new IllegalArgumentException(
                    "La hora de inicio debe ser anterior a la hora de fin");
        }
    }
}