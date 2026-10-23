package com.bembosevents.ai.dashboard;

public record AiDashboardSummary(
        long totalEventos,
        long exitosos,
        long errores,
        double tasaErrorPorcentaje,
        double duracionPromedioMs,
        long duracionMaximaMs,
        long eventosTool,
        long eventosProvider) {
}