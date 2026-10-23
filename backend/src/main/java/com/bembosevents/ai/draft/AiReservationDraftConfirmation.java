package com.bembosevents.ai.draft;

import com.bembosevents.dto.reserva.ReservaCreadaResponse;

public record AiReservationDraftConfirmation(
        AiReservationDraftResponse borrador,
        ReservaCreadaResponse reserva) {
}