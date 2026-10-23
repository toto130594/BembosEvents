package com.bembosevents.ai.orchestration;

import com.bembosevents.ai.draft.AiConfirmReservationDraftInput;
import com.bembosevents.ai.draft.AiDraftServiceSelection;
import com.bembosevents.ai.draft.AiReservationDraftPatch;
import com.bembosevents.ai.knowledge.KnowledgeQuery;
import com.bembosevents.ai.recommendation.AiRecommendationCriteria;
import com.bembosevents.ai.tool.AiAvailabilityInput;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class DeterministicAiIntentInterpreter
        implements AiIntentInterpreter {

    private static final Pattern SALON =
            Pattern.compile(
                    "\\bsalon\\s*(?:id\\s*)?#?\\s*(\\d+)\\b");

    private static final Pattern SEDE =
            Pattern.compile(
                    "\\bsede\\s*(?:id\\s*)?#?\\s*(\\d+)\\b");

    private static final Pattern PAQUETE =
            Pattern.compile(
                    "\\bpaquete\\s*(?:id\\s*)?#?\\s*(\\d+)\\b");

    private static final Pattern TEMATICA =
            Pattern.compile(
                    "\\btematica\\s*(?:id\\s*)?#?\\s*(\\d+)\\b");

    private static final Pattern INVITADOS =
            Pattern.compile(
                    "\\b(\\d+)\\s*(?:invitados|personas)\\b");

    private static final Pattern FECHA_ISO =
            Pattern.compile(
                    "\\b(\\d{4})-(\\d{1,2})-(\\d{1,2})\\b");

    private static final Pattern FECHA_LATAM =
            Pattern.compile(
                    "\\b(\\d{1,2})/(\\d{1,2})/(\\d{4})\\b");

    private static final Pattern HORARIO =
            Pattern.compile(
                    "\\b(?:de\\s+)?(\\d{1,2}):(\\d{2})\\s*(?:a|-)\\s*(\\d{1,2}):(\\d{2})\\b");

    private static final Pattern SERVICIO =
            Pattern.compile(
                    "\\bservicio\\s*(?:id\\s*)?#?\\s*(\\d+)\\s*(?:x|cantidad\\s*)\\s*(\\d+)\\b");

    @Override
    public AiIntent interpretar(
            String mensajeCliente) {

        if (mensajeCliente == null
                || mensajeCliente.isBlank()) {
            throw new IllegalArgumentException(
                    "El mensaje del cliente es obligatorio");
        }

        String normalized =
                normalizar(
                        mensajeCliente);

        if (esConfirmacionExplicita(
                normalized)) {

            return new AiIntent(
                    AiIntentType.CONFIRMAR_BORRADOR_RESERVA,
                    new AiConfirmReservationDraftInput());
        }

        AiIntent disponibilidad =
                intentarDisponibilidad(
                        normalized);

        if (disponibilidad != null) {
            return disponibilidad;
        }

        AiIntent recomendacion =
                intentarRecomendacion(
                        mensajeCliente,
                        normalized);

        if (recomendacion != null) {
            return recomendacion;
        }

        AiIntent borrador =
                intentarBorrador(
                        normalized);

        if (borrador != null) {
            return borrador;
        }

        if (esConsultaConocimiento(
                normalized)) {

            return new AiIntent(
                    AiIntentType.CONSULTAR_CONOCIMIENTO,
                    new KnowledgeQuery(
                            mensajeCliente.strip(),
                            3));
        }

        return AiIntent.general();
    }

    private AiIntent intentarDisponibilidad(
            String normalized) {

        if (!normalized.contains(
                "disponib")) {
            return null;
        }

        Integer idSalon =
                extraerEntero(
                        SALON,
                        normalized);

        LocalDate fecha =
                extraerFecha(
                        normalized);

        LocalTime[] horario =
                extraerHorario(
                        normalized);

        if (idSalon == null
                || fecha == null
                || horario == null) {
            return null;
        }

        return new AiIntent(
                AiIntentType.CONSULTAR_DISPONIBILIDAD,
                new AiAvailabilityInput(
                        idSalon,
                        fecha,
                        horario[0],
                        horario[1]));
    }

    private AiIntent intentarRecomendacion(
            String original,
            String normalized) {

        boolean asksRecommendation =
                normalized.contains("recomiend")
                        || normalized.contains("recomend")
                        || normalized.contains("que paquete")
                        || normalized.contains("cual paquete");

        if (!asksRecommendation) {
            return null;
        }

        Integer invitados =
                extraerEntero(
                        INVITADOS,
                        normalized);

        if (invitados == null) {
            return null;
        }

        return new AiIntent(
                AiIntentType.RECOMENDAR_CATALOGO,
                new AiRecommendationCriteria(
                        invitados,
                        original.strip(),
                        3));
    }

    private AiIntent intentarBorrador(
            String normalized) {

        boolean draftSignal =
                normalized.contains("borrador")
                        || normalized.contains("para mi reserva")
                        || normalized.contains("mi reserva sera")
                        || normalized.contains("mi reserva es");

        if (!draftSignal) {
            return null;
        }

        Integer idSede =
                extraerEntero(
                        SEDE,
                        normalized);

        Integer idSalon =
                extraerEntero(
                        SALON,
                        normalized);

        Integer idPaquete =
                extraerEntero(
                        PAQUETE,
                        normalized);

        Integer idTematica =
                extraerEntero(
                        TEMATICA,
                        normalized);

        Integer invitados =
                extraerEntero(
                        INVITADOS,
                        normalized);

        LocalDate fecha =
                extraerFecha(
                        normalized);

        LocalTime[] horario =
                extraerHorario(
                        normalized);

        List<AiDraftServiceSelection> services =
                extraerServicios(
                        normalized);

        boolean limpiarPaquete =
                normalized.contains("sin paquete");

        boolean limpiarTematica =
                normalized.contains("sin tematica");

        boolean limpiarServicios =
                normalized.contains("sin servicios");

        boolean hasAny =
                idSede != null
                        || idSalon != null
                        || idPaquete != null
                        || idTematica != null
                        || invitados != null
                        || fecha != null
                        || horario != null
                        || !services.isEmpty()
                        || limpiarPaquete
                        || limpiarTematica
                        || limpiarServicios;

        if (!hasAny) {
            return null;
        }

        return new AiIntent(
                AiIntentType.ACTUALIZAR_BORRADOR_RESERVA,
                new AiReservationDraftPatch(
                        idSede,
                        idSalon,
                        idPaquete,
                        idTematica,
                        fecha,
                        horario == null
                                ? null
                                : horario[0],
                        horario == null
                                ? null
                                : horario[1],
                        invitados,
                        services.isEmpty()
                                ? null
                                : services,
                        limpiarPaquete,
                        limpiarTematica,
                        limpiarServicios));
    }

    private boolean esConfirmacionExplicita(
            String normalized) {

        return normalized.matches(
                ".*\\b(confirmar borrador|confirmo mi reserva|confirmar mi reserva|confirmar reserva ahora|crear reserva ahora)\\b.*");
    }

    private boolean esConsultaConocimiento(
            String normalized) {

        boolean questionSignal =
                normalized.contains("?")
                        || normalized.startsWith("como ")
                        || normalized.startsWith("que ")
                        || normalized.startsWith("cual ")
                        || normalized.startsWith("puedo ")
                        || normalized.startsWith("cuando ");

        if (!questionSignal) {
            return false;
        }

        return normalized.contains("reserva")
                || normalized.contains("salon")
                || normalized.contains("sede")
                || normalized.contains("paquete")
                || normalized.contains("tematica")
                || normalized.contains("servicio")
                || normalized.contains("chat")
                || normalized.contains("atencion")
                || normalized.contains("disponib")
                || normalized.contains("backend");
    }

    private Integer extraerEntero(
            Pattern pattern,
            String value) {

        Matcher matcher =
                pattern.matcher(
                        value);

        if (!matcher.find()) {
            return null;
        }

        try {
            return Integer.valueOf(
                    matcher.group(1));
        }
        catch (NumberFormatException ex) {
            return null;
        }
    }

    private LocalDate extraerFecha(
            String value) {

        Matcher iso =
                FECHA_ISO.matcher(
                        value);

        if (iso.find()) {
            try {
                return LocalDate.of(
                        Integer.parseInt(
                                iso.group(1)),
                        Integer.parseInt(
                                iso.group(2)),
                        Integer.parseInt(
                                iso.group(3)));
            }
            catch (DateTimeException ex) {
                return null;
            }
        }

        Matcher latam =
                FECHA_LATAM.matcher(
                        value);

        if (latam.find()) {
            try {
                return LocalDate.of(
                        Integer.parseInt(
                                latam.group(3)),
                        Integer.parseInt(
                                latam.group(2)),
                        Integer.parseInt(
                                latam.group(1)));
            }
            catch (DateTimeException ex) {
                return null;
            }
        }

        return null;
    }

    private LocalTime[] extraerHorario(
            String value) {

        Matcher matcher =
                HORARIO.matcher(
                        value);

        if (!matcher.find()) {
            return null;
        }

        try {
            LocalTime inicio =
                    LocalTime.of(
                            Integer.parseInt(
                                    matcher.group(1)),
                            Integer.parseInt(
                                    matcher.group(2)));

            LocalTime fin =
                    LocalTime.of(
                            Integer.parseInt(
                                    matcher.group(3)),
                            Integer.parseInt(
                                    matcher.group(4)));

            if (!inicio.isBefore(fin)) {
                return null;
            }

            return new LocalTime[]{
                    inicio,
                    fin
            };
        }
        catch (DateTimeException ex) {
            return null;
        }
    }

    private List<AiDraftServiceSelection> extraerServicios(
            String value) {

        Matcher matcher =
                SERVICIO.matcher(
                        value);

        List<AiDraftServiceSelection> selections =
                new ArrayList<>();

        while (matcher.find()) {
            selections.add(
                    new AiDraftServiceSelection(
                            Integer.valueOf(
                                    matcher.group(1)),
                            Integer.valueOf(
                                    matcher.group(2))));
        }

        return List.copyOf(
                selections);
    }

    private String normalizar(
            String value) {

        String normalized =
                Normalizer.normalize(
                        value,
                        Normalizer.Form.NFD);

        return normalized
                .replaceAll(
                        "\\p{M}+",
                        "")
                .toLowerCase(
                        Locale.ROOT)
                .strip();
    }
}