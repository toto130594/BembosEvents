package com.bembosevents.ai.analytics;

import com.bembosevents.ai.orchestration.AiIntentType;
import com.bembosevents.ai.tool.AiToolType;

public record AiAnalyticsEventCommand(
        Integer idConversacion,
        Integer idSede,
        AiIntentType intentType,
        AiToolType toolType,
        AiAnalyticsRoute route,
        boolean exitoso,
        long durationMs,
        String errorType) {

    public AiAnalyticsEventCommand {
        if (idConversacion == null
                || idConversacion <= 0) {
            throw new IllegalArgumentException(
                    "La conversacion es obligatoria");
        }

        if (intentType == null) {
            throw new IllegalArgumentException(
                    "El intent es obligatorio");
        }

        if (route == null) {
            throw new IllegalArgumentException(
                    "La ruta de ejecucion es obligatoria");
        }

        if (durationMs < 0) {
            throw new IllegalArgumentException(
                    "La duracion no puede ser negativa");
        }

        if (route == AiAnalyticsRoute.TOOL
                && toolType == null) {
            throw new IllegalArgumentException(
                    "toolType es obligatorio para ruta TOOL");
        }

        if (route == AiAnalyticsRoute.PROVIDER
                && toolType != null) {
            throw new IllegalArgumentException(
                    "toolType debe ser null para ruta PROVIDER");
        }

        if (exitoso) {
            errorType = null;
        }
        else if (errorType != null) {
            errorType = errorType.strip();

            if (errorType.length() > 120) {
                errorType =
                        errorType.substring(
                                0,
                                120);
            }
        }
    }
}