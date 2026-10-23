package com.bembosevents.ai.tool;

import com.bembosevents.ai.recommendation.AiRecommendationCriteria;
import com.bembosevents.ai.recommendation.AiRecommendationResult;
import com.bembosevents.ai.recommendation.AiRecommendationService;
import com.bembosevents.dto.chat.ConversacionDetalleResponse;
import com.bembosevents.service.ChatClienteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiRecommendationToolTest {

    @Mock
    private ChatClienteService chatClienteService;

    @Mock
    private AiRecommendationService recommendationService;

    @Test
    void reutilizaOwnershipYDelegaAlMotorDeterministico() {
        AiRecommendationTool tool =
                new AiRecommendationTool(
                        chatClienteService,
                        recommendationService);

        AiToolContext context =
                new AiToolContext(
                        "cliente@bembos.test",
                        70);

        AiRecommendationCriteria criteria =
                new AiRecommendationCriteria(
                        30,
                        "superheroes",
                        3);

        AiRecommendationResult expected =
                new AiRecommendationResult(
                        30,
                        "superheroes",
                        List.of(),
                        List.of(),
                        List.of(),
                        "referencial");

        when(chatClienteService.obtenerPropia(
                "cliente@bembos.test",
                70))
                .thenReturn(
                        conversacion(
                                70,
                                "BOT"));

        when(recommendationService.recomendar(
                criteria))
                .thenReturn(
                        expected);

        AiRecommendationResult result =
                tool.ejecutar(
                        context,
                        criteria);

        assertSame(
                expected,
                result);

        verify(chatClienteService)
                .obtenerPropia(
                        "cliente@bembos.test",
                        70);

        verify(recommendationService)
                .recomendar(
                        criteria);
    }

    @Test
    void rechazaRecomendacionCuandoConversacionNoEstaEnBot() {
        AiRecommendationTool tool =
                new AiRecommendationTool(
                        chatClienteService,
                        recommendationService);

        AiRecommendationCriteria criteria =
                new AiRecommendationCriteria(
                        30,
                        "",
                        3);

        when(chatClienteService.obtenerPropia(
                "cliente@bembos.test",
                71))
                .thenReturn(
                        conversacion(
                                71,
                                "EN_ATENCION"));

        assertThrows(
                IllegalStateException.class,
                () -> tool.ejecutar(
                        new AiToolContext(
                                "cliente@bembos.test",
                                71),
                        criteria));

        verify(recommendationService, never())
                .recomendar(
                        criteria);
    }

    private ConversacionDetalleResponse conversacion(
            Integer idConversacion,
            String estado) {

        return new ConversacionDetalleResponse(
                idConversacion,
                7,
                3,
                estado,
                null,
                null,
                null,
                List.of());
    }
}