package com.bembosevents.ai.orchestration;

import com.bembosevents.ai.ChatAiProvider;
import com.bembosevents.ai.ChatAiRequest;
import com.bembosevents.ai.analytics.AiAnalyticsEventCommand;
import com.bembosevents.ai.analytics.AiAnalyticsRoute;
import com.bembosevents.ai.analytics.AiAnalyticsService;
import com.bembosevents.ai.security.AiSecurityDecision;
import com.bembosevents.ai.security.AiSecurityGuard;
import com.bembosevents.ai.draft.AiConfirmReservationDraftInput;
import com.bembosevents.ai.draft.AiReservationDraftConfirmation;
import com.bembosevents.ai.draft.AiReservationDraftPatch;
import com.bembosevents.ai.draft.AiReservationDraftResponse;
import com.bembosevents.ai.knowledge.KnowledgeHit;
import com.bembosevents.ai.knowledge.KnowledgeQuery;
import com.bembosevents.ai.knowledge.KnowledgeRetrievalResult;
import com.bembosevents.ai.recommendation.AiPackageRecommendation;
import com.bembosevents.ai.recommendation.AiRecommendationCriteria;
import com.bembosevents.ai.recommendation.AiRecommendationResult;
import com.bembosevents.ai.recommendation.AiServiceRecommendation;
import com.bembosevents.ai.recommendation.AiThemeRecommendation;
import com.bembosevents.ai.tool.AiAvailabilityInput;
import com.bembosevents.ai.tool.AiAvailabilityResult;
import com.bembosevents.ai.tool.AiToolContext;
import com.bembosevents.ai.tool.AiToolType;
import com.bembosevents.dto.reserva.ReservaCreadaResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class AiConversationOrchestrator {

    private final AiIntentInterpreter intentInterpreter;
    private final AiToolExecutor toolExecutor;
    private final ChatAiProvider chatAiProvider;
    private final AiAnalyticsService analyticsService;
    private final AiSecurityGuard securityGuard;

    public AiConversationOrchestrator(
            AiIntentInterpreter intentInterpreter,
            AiToolExecutor toolExecutor,
            ChatAiProvider chatAiProvider,
            AiAnalyticsService analyticsService,
            AiSecurityGuard securityGuard) {

        this.intentInterpreter =
                intentInterpreter;

        this.toolExecutor =
                toolExecutor;

        this.chatAiProvider =
                chatAiProvider;

        this.analyticsService =
                analyticsService;

        this.securityGuard =
                securityGuard;
    }

    public String responder(
            AiConversationRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "La solicitud conversacional es obligatoria");
        }

        AiSecurityDecision turnDecision =
                securityGuard.evaluarTurno(
                        request.correoCliente(),
                        request.idConversacion(),
                        request.mensajeCliente());

        if (!turnDecision.permitido()) {
            return turnDecision.respuestaSegura();
        }

        AiIntent intent =
                intentInterpreter.interpretar(
                        request.mensajeCliente());

        if (intent.tipo()
                == AiIntentType.CHAT_GENERAL) {

            AiSecurityDecision providerDecision =
                    securityGuard.evaluarPresupuestoProvider(
                            request.correoCliente(),
                            request.idConversacion());

            if (!providerDecision.permitido()) {
                return providerDecision.respuestaSegura();
            }
        }

        AiToolContext context =
                new AiToolContext(
                        request.correoCliente(),
                        request.idConversacion());

        long startedAt =
                System.nanoTime();

        try {
            String response =
                    switch (intent.tipo()) {
                        case CONSULTAR_DISPONIBILIDAD ->
                                renderDisponibilidad(
                                        toolExecutor.ejecutar(
                                                AiToolType.CONSULTAR_DISPONIBILIDAD,
                                                context,
                                                (AiAvailabilityInput)
                                                    intent.input(),
                                                AiAvailabilityResult.class));

                        case RECOMENDAR_CATALOGO ->
                                renderRecomendacion(
                                        toolExecutor.ejecutar(
                                                AiToolType.RECOMENDAR_CATALOGO,
                                                context,
                                                (AiRecommendationCriteria)
                                                    intent.input(),
                                                AiRecommendationResult.class));

                        case ACTUALIZAR_BORRADOR_RESERVA ->
                                renderBorrador(
                                        toolExecutor.ejecutar(
                                                AiToolType.ACTUALIZAR_BORRADOR_RESERVA,
                                                context,
                                                (AiReservationDraftPatch)
                                                    intent.input(),
                                                AiReservationDraftResponse.class));

                        case CONFIRMAR_BORRADOR_RESERVA ->
                                renderConfirmacion(
                                        toolExecutor.ejecutar(
                                                AiToolType.CONFIRMAR_BORRADOR_RESERVA,
                                                context,
                                                (AiConfirmReservationDraftInput)
                                                    intent.input(),
                                                AiReservationDraftConfirmation.class));

                        case CONSULTAR_CONOCIMIENTO ->
                                renderKnowledge(
                                        toolExecutor.ejecutar(
                                                AiToolType.CONSULTAR_CONOCIMIENTO,
                                                context,
                                                (KnowledgeQuery)
                                                    intent.input(),
                                                KnowledgeRetrievalResult.class));

                        case CHAT_GENERAL ->
                                responderGeneral(
                                        request);
                    };

            recordAnalyticsSafely(
                    request,
                    intent,
                    true,
                    elapsedMs(
                            startedAt),
                    null);

            return response;
        }
        catch (RuntimeException ex) {
            recordAnalyticsSafely(
                    request,
                    intent,
                    false,
                    elapsedMs(
                            startedAt),
                    ex.getClass()
                            .getSimpleName());

            throw ex;
        }
    }

    private void recordAnalyticsSafely(
            AiConversationRequest request,
            AiIntent intent,
            boolean exitoso,
            long durationMs,
            String errorType) {

        try {
            analyticsService.record(
                    new AiAnalyticsEventCommand(
                            request.idConversacion(),
                            request.idSede(),
                            intent.tipo(),
                            toolFor(
                                    intent.tipo()),
                            routeFor(
                                    intent.tipo()),
                            exitoso,
                            durationMs,
                            errorType));
        }
        catch (RuntimeException ignored) {
            // Analytics is best-effort and must never break chat.
        }
    }

    private AiAnalyticsRoute routeFor(
            AiIntentType type) {

        return type == AiIntentType.CHAT_GENERAL
                ? AiAnalyticsRoute.PROVIDER
                : AiAnalyticsRoute.TOOL;
    }

    private AiToolType toolFor(
            AiIntentType type) {

        return switch (type) {
            case CONSULTAR_DISPONIBILIDAD ->
                    AiToolType.CONSULTAR_DISPONIBILIDAD;

            case RECOMENDAR_CATALOGO ->
                    AiToolType.RECOMENDAR_CATALOGO;

            case ACTUALIZAR_BORRADOR_RESERVA ->
                    AiToolType.ACTUALIZAR_BORRADOR_RESERVA;

            case CONFIRMAR_BORRADOR_RESERVA ->
                    AiToolType.CONFIRMAR_BORRADOR_RESERVA;

            case CONSULTAR_CONOCIMIENTO ->
                    AiToolType.CONSULTAR_CONOCIMIENTO;

            case CHAT_GENERAL ->
                    null;
        };
    }

    private long elapsedMs(
            long startedAt) {

        long elapsed =
                System.nanoTime()
                        - startedAt;

        return Math.max(
                0L,
                elapsed / 1_000_000L);
    }

    private String responderGeneral(
            AiConversationRequest request) {

        String response =
                chatAiProvider.responder(
                        new ChatAiRequest(
                                request.idConversacion(),
                                request.idSede(),
                                request.mensajeCliente()));

        if (response == null
                || response.isBlank()) {
            throw new IllegalStateException(
                    "El proveedor IA devolvio una respuesta vacia");
        }

        return response.strip();
    }

    private String renderDisponibilidad(
            AiAvailabilityResult result) {

        return String.format(
                Locale.ROOT,
                "Disponibilidad consultada para %s (salon %d), %s de %s a %s: %s. Motivo: %s. Capacidad catalogada: %d.",
                result.nombreSalon(),
                result.idSalon(),
                result.fecha(),
                result.horaInicio(),
                result.horaFin(),
                result.disponible()
                        ? "DISPONIBLE"
                        : "NO DISPONIBLE",
                result.motivo(),
                result.capacidad());
    }

    private String renderRecomendacion(
            AiRecommendationResult result) {

        StringBuilder builder =
                new StringBuilder();

        builder.append(
                "Recomendacion referencial para ")
                .append(
                        result.numeroInvitados())
                .append(
                        " invitados.");

        if (result.paquetes() == null
                || result.paquetes().isEmpty()) {

            builder.append(
                    " No encontre paquetes activos para recomendar.");
        }
        else {
            builder.append(
                    " Paquetes: ");

            builder.append(
                    result.paquetes()
                            .stream()
                            .map(this::renderPaquete)
                            .collect(
                                    Collectors.joining(
                                            " | ")));

            builder.append(".");
        }

        if (result.tematicas() != null
                && !result.tematicas().isEmpty()) {

            builder.append(
                    " Tematicas: ");

            builder.append(
                    result.tematicas()
                            .stream()
                            .map(this::renderTematica)
                            .collect(
                                    Collectors.joining(
                                            " | ")));

            builder.append(".");
        }

        if (result.serviciosAdicionales() != null
                && !result.serviciosAdicionales().isEmpty()) {

            builder.append(
                    " Servicios: ");

            builder.append(
                    result.serviciosAdicionales()
                            .stream()
                            .map(this::renderServicio)
                            .collect(
                                    Collectors.joining(
                                            " | ")));

            builder.append(".");
        }

        builder.append(" ")
                .append(
                        result.aviso());

        return builder
                .toString()
                .strip();
    }

    private String renderPaquete(
            AiPackageRecommendation item) {

        return String.format(
                Locale.ROOT,
                "%s (ID %d, precio catalogo %s, base %d personas)",
                item.nombre(),
                item.idPaquete(),
                item.precioBase(),
                item.cantidadPersonasBase());
    }

    private String renderTematica(
            AiThemeRecommendation item) {

        return String.format(
                Locale.ROOT,
                "%s (ID %d, adicional catalogo %s)",
                item.nombre(),
                item.idTematica(),
                item.precioAdicional());
    }

    private String renderServicio(
            AiServiceRecommendation item) {

        return String.format(
                Locale.ROOT,
                "%s (ID %d, precio unitario catalogo %s)",
                item.nombre(),
                item.idServicioAdicional(),
                item.precioUnitario());
    }

    private String renderBorrador(
            AiReservationDraftResponse draft) {

        String faltantes =
                draft.camposFaltantes() == null
                        || draft.camposFaltantes()
                                .isEmpty()
                        ? "ninguno"
                        : String.join(
                                ", ",
                                draft.camposFaltantes());

        return String.format(
                Locale.ROOT,
                "Borrador de reserva actualizado. Estado: %s. Sede: %s. Salon: %s. Fecha: %s. Horario: %s-%s. Invitados: %s. Listo para confirmar: %s. Campos faltantes: %s.",
                draft.estado(),
                value(draft.idSede()),
                value(draft.idSalon()),
                value(draft.fechaEvento()),
                value(draft.horaInicio()),
                value(draft.horaFin()),
                value(draft.cantidadInvitados()),
                draft.listoParaConfirmar()
                        ? "SI"
                        : "NO",
                faltantes);
    }

    private String renderConfirmacion(
            AiReservationDraftConfirmation confirmation) {

        ReservaCreadaResponse reserva =
                confirmation.reserva();

        if (reserva == null) {
            throw new IllegalStateException(
                    "La confirmacion no devolvio una reserva");
        }

        return String.format(
                Locale.ROOT,
                "Reserva creada correctamente. Codigo: %s. ID: %d. Estado: %s. Fecha: %s. Horario: %s-%s. Invitados: %d. Monto total: %s.",
                reserva.codigoReserva(),
                reserva.idReserva(),
                reserva.estado(),
                reserva.fechaEvento(),
                reserva.horaInicio(),
                reserva.horaFin(),
                reserva.cantidadInvitados(),
                reserva.montoTotal());
    }

    private String renderKnowledge(
            KnowledgeRetrievalResult result) {

        if (!result.tieneEvidencia()) {
            return "No encontre informacion verificable en la base de conocimiento publica para responder esa consulta.";
        }

        String evidence =
                result.evidencias()
                        .stream()
                        .limit(3)
                        .map(this::renderKnowledgeHit)
                        .collect(
                                Collectors.joining(
                                        " "));

        String sources =
                result.evidencias()
                        .stream()
                        .limit(3)
                        .map(
                                KnowledgeHit::sourceUri)
                        .distinct()
                        .collect(
                                Collectors.joining(
                                        ", "));

        return "Informacion verificada: "
                + evidence
                + " Fuentes: "
                + sources;
    }

    private String renderKnowledgeHit(
            KnowledgeHit hit) {

        return hit.fragmento()
                .strip();
    }

    private String value(
            Object value) {

        return value == null
                ? "pendiente"
                : String.valueOf(
                        value);
    }
}