package com.bembosevents.ai.orchestration;

public record AiConversationRequest(
        String correoCliente,
        Integer idConversacion,
        Integer idSede,
        String mensajeCliente) {

    public AiConversationRequest {
        if (correoCliente == null
                || correoCliente.isBlank()) {
            throw new IllegalArgumentException(
                    "El correo del cliente es obligatorio");
        }

        if (idConversacion == null
                || idConversacion <= 0) {
            throw new IllegalArgumentException(
                    "La conversacion es obligatoria");
        }

        if (mensajeCliente == null
                || mensajeCliente.isBlank()) {
            throw new IllegalArgumentException(
                    "El mensaje del cliente es obligatorio");
        }

        correoCliente = correoCliente.strip();
        mensajeCliente = mensajeCliente.strip();
    }
}