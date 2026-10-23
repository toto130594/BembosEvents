package com.bembosevents.ai.orchestration;

public record AiIntent(
        AiIntentType tipo,
        Object input) {

    public AiIntent {
        if (tipo == null) {
            throw new IllegalArgumentException(
                    "El tipo de intent es obligatorio");
        }

        if (tipo != AiIntentType.CHAT_GENERAL
                && input == null) {
            throw new IllegalArgumentException(
                    "El input es obligatorio para intents de tool");
        }
    }

    public static AiIntent general() {
        return new AiIntent(
                AiIntentType.CHAT_GENERAL,
                null);
    }
}