package com.bembosevents.ai.recommendation;

import java.util.List;

public record AiRecommendationResult(
        Integer numeroInvitados,
        String preferencias,
        List<AiPackageRecommendation> paquetes,
        List<AiThemeRecommendation> tematicas,
        List<AiServiceRecommendation> serviciosAdicionales,
        String aviso) {
}