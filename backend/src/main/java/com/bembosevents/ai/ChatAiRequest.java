package com.bembosevents.ai;

public record ChatAiRequest(
        Integer idConversacion,
        Integer idSede,
        String mensajeCliente
) {
}