package com.bembosevents.ai.draft;

public interface AiReservationDraftService {

    AiReservationDraftResponse obtenerOCrear(
            String correoCliente,
            Integer idConversacion);

    AiReservationDraftResponse actualizar(
            String correoCliente,
            Integer idConversacion,
            AiReservationDraftPatch patch);

    AiReservationDraftConfirmation confirmar(
            String correoCliente,
            Integer idConversacion);
}