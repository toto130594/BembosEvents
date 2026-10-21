package com.bembosevents.dto.chat;

import java.time.LocalDateTime;

public record ConversacionResumenResponse(
        Integer idConversacion,
        Integer idSede,
        String estado,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion,
        LocalDateTime fechaCierre
) {
}