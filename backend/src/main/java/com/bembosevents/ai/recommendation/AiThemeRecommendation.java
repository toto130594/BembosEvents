package com.bembosevents.ai.recommendation;

import java.math.BigDecimal;

public record AiThemeRecommendation(
        Integer idTematica,
        String nombre,
        String descripcion,
        BigDecimal precioAdicional,
        Integer coincidenciasPreferencia,
        String motivo) {
}