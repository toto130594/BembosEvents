package com.bembosevents.ai.dashboard;

public record AiDashboardBreakdownItem(
        String clave,
        long total,
        long errores,
        double tasaErrorPorcentaje,
        double duracionPromedioMs) {
}