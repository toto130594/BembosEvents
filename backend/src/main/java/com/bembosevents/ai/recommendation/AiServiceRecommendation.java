package com.bembosevents.ai.recommendation;

import java.math.BigDecimal;

public record AiServiceRecommendation(
        Integer idServicioAdicional,
        String nombre,
        String descripcion,
        BigDecimal precioUnitario,
        Integer coincidenciasPreferencia,
        String motivo) {
}