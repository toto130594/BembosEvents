package com.bembosevents.ai.tool;

import com.bembosevents.ai.recommendation.AiRecommendationCriteria;
import com.bembosevents.ai.recommendation.AiRecommendationResult;
import com.bembosevents.ai.recommendation.AiRecommendationService;
import com.bembosevents.dto.chat.ConversacionDetalleResponse;
import com.bembosevents.service.ChatClienteService;
import org.springframework.stereotype.Component;

@Component
public class AiRecommendationTool
        implements AiTool<AiRecommendationCriteria, AiRecommendationResult> {

    private final ChatClienteService chatClienteService;
    private final AiRecommendationService recommendationService;

    public AiRecommendationTool(
            ChatClienteService chatClienteService,
            AiRecommendationService recommendationService) {

        this.chatClienteService =
                chatClienteService;

        this.recommendationService =
                recommendationService;
    }

    @Override
    public AiToolType type() {
        return AiToolType.RECOMENDAR_CATALOGO;
    }

    @Override
    public Class<AiRecommendationCriteria> inputType() {
        return AiRecommendationCriteria.class;
    }

    @Override
    public AiRecommendationResult ejecutar(
            AiToolContext context,
            AiRecommendationCriteria input) {

        if (context == null) {
            throw new IllegalArgumentException(
                    "El contexto de IA es obligatorio");
        }

        if (input == null) {
            throw new IllegalArgumentException(
                    "Los criterios de recomendacion son obligatorios");
        }

        ConversacionDetalleResponse conversacion =
                chatClienteService.obtenerPropia(
                        context.correoCliente(),
                        context.idConversacion());

        if (!"BOT".equals(conversacion.estado())) {
            throw new IllegalStateException(
                    "La IA solo puede recomendar mientras la conversacion esta en BOT");
        }

        return recommendationService.recomendar(
                input);
    }
}