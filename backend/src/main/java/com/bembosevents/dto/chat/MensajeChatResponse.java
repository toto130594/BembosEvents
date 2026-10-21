package com.bembosevents.dto.chat;

import java.time.LocalDateTime;

public record MensajeChatResponse(
        Integer idMensajeChat,
        String tipoEmisor,
        Integer idUsuarioEmisor,
        String contenido,
        LocalDateTime fechaEnvio
) {
}