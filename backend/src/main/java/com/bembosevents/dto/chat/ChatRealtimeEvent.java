package com.bembosevents.dto.chat;

public record ChatRealtimeEvent(
        String tipo,
        Integer idConversacion,
        MensajeChatResponse mensaje
) {

    public static ChatRealtimeEvent mensaje(
            Integer idConversacion,
            MensajeChatResponse mensaje) {

        return new ChatRealtimeEvent(
                "MENSAJE",
                idConversacion,
                mensaje
        );
    }
}