package com.bembosevents.dto.chat;

public record ChatEstadoRealtimeEvent(
        Integer idConversacion,
        String estado
) {
}