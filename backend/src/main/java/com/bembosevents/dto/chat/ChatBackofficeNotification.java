package com.bembosevents.dto.chat;

import java.time.LocalDateTime;

public record ChatBackofficeNotification(
        String tipo,
        Integer idConversacion,
        Integer idSede,
        String estado,
        String mensaje,
        LocalDateTime fecha
) {
}