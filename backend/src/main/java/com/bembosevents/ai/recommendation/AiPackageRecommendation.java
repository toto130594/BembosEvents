package com.bembosevents.ai.recommendation;

import java.math.BigDecimal;

public record AiPackageRecommendation(
        Integer idPaquete,
        String nombre,
        String descripcion,
        BigDecimal precioBase,
        Integer cantidadPersonasBase,
        Integer diferenciaPersonas,
        Integer coincidenciasPreferencia,
        String motivo) {
}