package com.bembosevents.ai.tool;

public record AiToolContext(
        String correoCliente,
        Integer idConversacion) {

    public AiToolContext {
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

        correoCliente = correoCliente.strip();
    }
}