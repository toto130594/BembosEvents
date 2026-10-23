package com.bembosevents.ai.dashboard;

import com.bembosevents.entity.AiAnalyticsEvent;
import com.bembosevents.repository.AiAnalyticsEventRepository;
import com.bembosevents.service.SedeAccessService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

@Service
@Transactional(readOnly = true)
public class AiBackofficeDashboardService {

    private static final long MAX_RANGE_DAYS = 31;

    private final AiAnalyticsEventRepository analyticsRepository;
    private final SedeAccessService sedeAccessService;

    public AiBackofficeDashboardService(
            AiAnalyticsEventRepository analyticsRepository,
            SedeAccessService sedeAccessService) {

        this.analyticsRepository =
                analyticsRepository;

        this.sedeAccessService =
                sedeAccessService;
    }

    public AiBackofficeDashboardResponse obtener(
            LocalDate desde,
            LocalDate hasta,
            Integer idSedeSolicitada,
            Authentication authentication) {

        validarFechas(
                desde,
                hasta);

        Scope scope =
                resolverScope(
                        idSedeSolicitada,
                        authentication);

        LocalDateTime desdeInclusive =
                desde.atStartOfDay();

        LocalDateTime hastaExclusive =
                hasta.plusDays(1)
                        .atStartOfDay();

        List<AiAnalyticsEvent> eventos =
                scope.idSede() == null
                        ? analyticsRepository
                                .findAllByFechaEventoGreaterThanEqualAndFechaEventoLessThanOrderByFechaEventoAsc(
                                        desdeInclusive,
                                        hastaExclusive)
                        : analyticsRepository
                                .findAllByIdSedeAndFechaEventoGreaterThanEqualAndFechaEventoLessThanOrderByFechaEventoAsc(
                                        scope.idSede(),
                                        desdeInclusive,
                                        hastaExclusive);

        AiDashboardSummary summary =
                resumen(
                        eventos);

        List<AiDashboardBreakdownItem> intents =
                breakdown(
                        eventos,
                        AiAnalyticsEvent::getIntentType);

        List<AiDashboardBreakdownItem> tools =
                breakdown(
                        eventos.stream()
                                .filter(event ->
                                        event.getToolType() != null)
                                .toList(),
                        AiAnalyticsEvent::getToolType);

        List<AiDashboardBreakdownItem> rutas =
                breakdown(
                        eventos,
                        AiAnalyticsEvent::getExecutionRoute);

        List<AiDashboardInsight> insights =
                insights(
                        summary,
                        intents,
                        tools,
                        rutas);

        return new AiBackofficeDashboardResponse(
                desde,
                hasta,
                scope.idSede(),
                scope.global(),
                summary,
                intents,
                tools,
                rutas,
                insights);
    }

    private void validarFechas(
            LocalDate desde,
            LocalDate hasta) {

        if (desde == null
                || hasta == null) {
            throw new IllegalArgumentException(
                    "desde y hasta son obligatorios");
        }

        if (hasta.isBefore(
                desde)) {
            throw new IllegalArgumentException(
                    "hasta no puede ser anterior a desde");
        }

        long days =
                ChronoUnit.DAYS.between(
                        desde,
                        hasta) + 1;

        if (days > MAX_RANGE_DAYS) {
            throw new IllegalArgumentException(
                    "El dashboard permite como maximo 31 dias por consulta");
        }
    }

    private Scope resolverScope(
            Integer idSedeSolicitada,
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()) {
            throw new AccessDeniedException(
                    "Autenticacion requerida");
        }

        if (sedeAccessService.esAdministrador(
                authentication)) {

            if (idSedeSolicitada != null) {
                sedeAccessService.validarAccesoSede(
                        authentication,
                        idSedeSolicitada);

                return new Scope(
                        idSedeSolicitada,
                        false);
            }

            return new Scope(
                    null,
                    true);
        }

        if (!sedeAccessService.esEncargadoLocal(
                authentication)) {
            throw new AccessDeniedException(
                    "Rol BackOffice requerido");
        }

        Integer assigned =
                sedeAccessService.obtenerSedeAsignada(
                        authentication.getName());

        if (idSedeSolicitada != null) {
            sedeAccessService.validarAccesoSede(
                    authentication,
                    idSedeSolicitada);

            if (!assigned.equals(
                    idSedeSolicitada)) {
                throw new AccessDeniedException(
                        "Sede fuera del alcance del encargado");
            }
        }

        return new Scope(
                assigned,
                false);
    }

    private AiDashboardSummary resumen(
            List<AiAnalyticsEvent> eventos) {

        long total =
                eventos.size();

        long exitosos =
                eventos.stream()
                        .filter(event ->
                                Boolean.TRUE.equals(
                                        event.getExitoso()))
                        .count();

        long errores =
                total - exitosos;

        long tool =
                eventos.stream()
                        .filter(event ->
                                "TOOL".equals(
                                        event.getExecutionRoute()))
                        .count();

        long provider =
                eventos.stream()
                        .filter(event ->
                                "PROVIDER".equals(
                                        event.getExecutionRoute()))
                        .count();

        double avg =
                eventos.stream()
                        .map(
                                AiAnalyticsEvent::getDurationMs)
                        .filter(value ->
                                value != null)
                        .mapToLong(
                                Long::longValue)
                        .average()
                        .orElse(0.0);

        long max =
                eventos.stream()
                        .map(
                                AiAnalyticsEvent::getDurationMs)
                        .filter(value ->
                                value != null)
                        .mapToLong(
                                Long::longValue)
                        .max()
                        .orElse(0L);

        return new AiDashboardSummary(
                total,
                exitosos,
                errores,
                percentage(
                        errores,
                        total),
                round2(avg),
                max,
                tool,
                provider);
    }

    private List<AiDashboardBreakdownItem> breakdown(
            List<AiAnalyticsEvent> eventos,
            Function<AiAnalyticsEvent, String> classifier) {

        Map<String, List<AiAnalyticsEvent>> grouped =
                new LinkedHashMap<>();

        eventos.stream()
                .sorted(
                        Comparator.comparing(
                                event ->
                                        classifier.apply(
                                                event)))
                .forEach(event -> {
                    String key =
                            classifier.apply(
                                    event);

                    if (key == null
                            || key.isBlank()) {
                        return;
                    }

                    grouped.computeIfAbsent(
                                    key,
                                    ignored ->
                                            new ArrayList<>())
                            .add(
                                    event);
                });

        return grouped.entrySet()
                .stream()
                .map(entry ->
                        breakdownItem(
                                entry.getKey(),
                                entry.getValue()))
                .sorted(
                        Comparator
                                .comparingLong(
                                        AiDashboardBreakdownItem::total)
                                .reversed()
                                .thenComparing(
                                        AiDashboardBreakdownItem::clave))
                .toList();
    }

    private AiDashboardBreakdownItem breakdownItem(
            String key,
            List<AiAnalyticsEvent> eventos) {

        long total =
                eventos.size();

        long errores =
                eventos.stream()
                        .filter(event ->
                                !Boolean.TRUE.equals(
                                        event.getExitoso()))
                        .count();

        double avg =
                eventos.stream()
                        .map(
                                AiAnalyticsEvent::getDurationMs)
                        .filter(value ->
                                value != null)
                        .mapToLong(
                                Long::longValue)
                        .average()
                        .orElse(0.0);

        return new AiDashboardBreakdownItem(
                key,
                total,
                errores,
                percentage(
                        errores,
                        total),
                round2(avg));
    }

    private List<AiDashboardInsight> insights(
            AiDashboardSummary summary,
            List<AiDashboardBreakdownItem> intents,
            List<AiDashboardBreakdownItem> tools,
            List<AiDashboardBreakdownItem> routes) {

        List<AiDashboardInsight> result =
                new ArrayList<>();

        if (summary.totalEventos() == 0) {
            result.add(
                    new AiDashboardInsight(
                            "SIN_DATOS",
                            "No hay eventos de IA en el periodo seleccionado."));

            return List.copyOf(
                    result);
        }

        if (!intents.isEmpty()) {
            AiDashboardBreakdownItem top =
                    intents.get(0);

            result.add(
                    new AiDashboardInsight(
                            "TOP_INTENT",
                            "El intent mas utilizado fue "
                                    + top.clave()
                                    + " con "
                                    + top.total()
                                    + " eventos."));
        }

        if (!tools.isEmpty()) {
            AiDashboardBreakdownItem topTool =
                    tools.get(0);

            result.add(
                    new AiDashboardInsight(
                            "TOP_TOOL",
                            "El tool mas utilizado fue "
                                    + topTool.clave()
                                    + " con "
                                    + topTool.total()
                                    + " ejecuciones."));
        }

        if (summary.tasaErrorPorcentaje() >= 10.0) {
            result.add(
                    new AiDashboardInsight(
                            "ERROR_RATE_HIGH",
                            String.format(
                                    Locale.ROOT,
                                    "La tasa de error es %.2f%% y requiere revision.",
                                    summary.tasaErrorPorcentaje())));
        }
        else {
            result.add(
                    new AiDashboardInsight(
                            "ERROR_RATE",
                            String.format(
                                    Locale.ROOT,
                                    "La tasa de error del periodo es %.2f%%.",
                                    summary.tasaErrorPorcentaje())));
        }

        if (summary.eventosProvider() > summary.eventosTool()) {
            result.add(
                    new AiDashboardInsight(
                            "PROVIDER_USAGE",
                            "El chat general via provider supera el uso de tools deterministas."));
        }
        else {
            result.add(
                    new AiDashboardInsight(
                            "TOOL_USAGE",
                            "Los tools deterministas representan la mayor parte o igualan el trafico IA."));
        }

        if (summary.duracionPromedioMs() >= 1500.0) {
            result.add(
                    new AiDashboardInsight(
                            "LATENCY_HIGH",
                            String.format(
                                    Locale.ROOT,
                                    "La latencia promedio es %.2f ms y conviene revisarla.",
                                    summary.duracionPromedioMs())));
        }
        else {
            result.add(
                    new AiDashboardInsight(
                            "LATENCY",
                            String.format(
                                    Locale.ROOT,
                                    "La latencia promedio es %.2f ms.",
                                    summary.duracionPromedioMs())));
        }

        return List.copyOf(
                result);
    }

    private double percentage(
            long numerator,
            long denominator) {

        if (denominator <= 0) {
            return 0.0;
        }

        return round2(
                ((double) numerator * 100.0)
                        / denominator);
    }

    private double round2(
            double value) {

        return Math.round(
                value * 100.0)
                / 100.0;
    }

    private record Scope(
            Integer idSede,
            boolean global) {
    }
}