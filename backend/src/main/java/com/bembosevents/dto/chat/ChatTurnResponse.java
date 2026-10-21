package com.bembosevents.dto.chat;

public record ChatTurnResponse(
        MensajeChatResponse cliente,
        MensajeChatResponse ia
) {
}