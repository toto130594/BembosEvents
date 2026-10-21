package com.bembosevents.dto.chat;

import java.time.LocalDateTime;
import java.util.List;

public record ConversacionDetalleResponse(
        Integer idConversacion,
        Integer idCliente,
        Integer idSede,
        String estado,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion,
        LocalDateTime fechaCierre,
        List<MensajeChatResponse> mensajes
) {
}