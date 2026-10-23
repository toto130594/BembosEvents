package com.bembosevents.ai.recommendation;

public record AiRecommendationCriteria(
        Integer numeroInvitados,
        String preferencias,
        Integer maxResultados) {

    public AiRecommendationCriteria {
        if (numeroInvitados == null
                || numeroInvitados <= 0) {
            throw new IllegalArgumentException(
                    "El numero de invitados debe ser mayor que cero");
        }

        if (preferencias == null) {
            preferencias = "";
        }
        else {
            preferencias = preferencias.strip();
        }

        if (maxResultados == null) {
            maxResultados = 3;
        }

        if (maxResultados < 1
                || maxResultados > 5) {
            throw new IllegalArgumentException(
                    "maxResultados debe estar entre 1 y 5");
        }
    }
}