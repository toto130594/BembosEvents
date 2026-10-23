package com.bembosevents.ai.dashboard;

import java.time.LocalDate;
import java.util.List;

public record AiBackofficeDashboardResponse(
        LocalDate desde,
        LocalDate hasta,
        Integer idSede,
        boolean alcanceGlobal,
        AiDashboardSummary resumen,
        List<AiDashboardBreakdownItem> intents,
        List<AiDashboardBreakdownItem> tools,
        List<AiDashboardBreakdownItem> rutas,
        List<AiDashboardInsight> insights) {

    public AiBackofficeDashboardResponse {
        intents =
                intents == null
                        ? List.of()
                        : List.copyOf(intents);

        tools =
                tools == null
                        ? List.of()
                        : List.copyOf(tools);

        rutas =
                rutas == null
                        ? List.of()
                        : List.copyOf(rutas);

        insights =
                insights == null
                        ? List.of()
                        : List.copyOf(insights);
    }
}