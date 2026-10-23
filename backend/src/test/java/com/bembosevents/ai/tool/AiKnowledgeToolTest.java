package com.bembosevents.ai.tool;

import com.bembosevents.ai.knowledge.KnowledgeQuery;
import com.bembosevents.ai.knowledge.KnowledgeRetrievalResult;
import com.bembosevents.ai.knowledge.KnowledgeRetriever;
import com.bembosevents.dto.chat.ConversacionDetalleResponse;
import com.bembosevents.service.ChatClienteService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiKnowledgeToolTest {

    @Test
    void reutilizaOwnershipBotYDelegaRetriever() {
        ChatClienteService chatClienteService =
                mock(
                        ChatClienteService.class);

        KnowledgeRetriever retriever =
                mock(
                        KnowledgeRetriever.class);

        AiKnowledgeTool tool =
                new AiKnowledgeTool(
                        chatClienteService,
                        retriever);

        AiToolContext context =
                new AiToolContext(
                        "cliente@bembos.test",
                        10);

        KnowledgeQuery query =
                new KnowledgeQuery(
                        "Como funciona una reserva?",
                        3);

        KnowledgeRetrievalResult expected =
                new KnowledgeRetrievalResult(
                        query.pregunta(),
                        List.of());

        when(chatClienteService.obtenerPropia(
                "cliente@bembos.test",
                10))
                .thenReturn(
                        conversacion(
                                10,
                                "BOT"));

        when(retriever.recuperar(
                query))
                .thenReturn(
                        expected);

        KnowledgeRetrievalResult result =
                tool.ejecutar(
                        context,
                        query);

        assertSame(
                expected,
                result);

        verify(retriever)
                .recuperar(
                        query);
    }

    @Test
    void noConsultaCorpusSiConversacionNoEstaEnBot() {
        ChatClienteService chatClienteService =
                mock(
                        ChatClienteService.class);

        KnowledgeRetriever retriever =
                mock(
                        KnowledgeRetriever.class);

        AiKnowledgeTool tool =
                new AiKnowledgeTool(
                        chatClienteService,
                        retriever);

        KnowledgeQuery query =
                new KnowledgeQuery(
                        "Como funciona una reserva?",
                        3);

        when(chatClienteService.obtenerPropia(
                "cliente@bembos.test",
                10))
                .thenReturn(
                        conversacion(
                                10,
                                "EN_ATENCION"));

        assertThrows(
                IllegalStateException.class,
                () -> tool.ejecutar(
                        new AiToolContext(
                                "cliente@bembos.test",
                                10),
                        query));

        verify(retriever, never())
                .recuperar(
                        query);
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