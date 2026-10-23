package com.bembosevents.ai.tool;

import com.bembosevents.ai.knowledge.KnowledgeQuery;
import com.bembosevents.ai.knowledge.KnowledgeRetrievalResult;
import com.bembosevents.ai.knowledge.KnowledgeRetriever;
import com.bembosevents.dto.chat.ConversacionDetalleResponse;
import com.bembosevents.service.ChatClienteService;
import org.springframework.stereotype.Component;

@Component
public class AiKnowledgeTool
        implements AiTool<KnowledgeQuery, KnowledgeRetrievalResult> {

    private final ChatClienteService chatClienteService;
    private final KnowledgeRetriever knowledgeRetriever;

    public AiKnowledgeTool(
            ChatClienteService chatClienteService,
            KnowledgeRetriever knowledgeRetriever) {

        this.chatClienteService =
                chatClienteService;

        this.knowledgeRetriever =
                knowledgeRetriever;
    }

    @Override
    public AiToolType type() {
        return AiToolType.CONSULTAR_CONOCIMIENTO;
    }

    @Override
    public Class<KnowledgeQuery> inputType() {
        return KnowledgeQuery.class;
    }

    @Override
    public KnowledgeRetrievalResult ejecutar(
            AiToolContext context,
            KnowledgeQuery input) {

        if (context == null) {
            throw new IllegalArgumentException(
                    "El contexto de IA es obligatorio");
        }

        if (input == null) {
            throw new IllegalArgumentException(
                    "La consulta de conocimiento es obligatoria");
        }

        ConversacionDetalleResponse conversacion =
                chatClienteService.obtenerPropia(
                        context.correoCliente(),
                        context.idConversacion());

        if (!"BOT".equals(
                conversacion.estado())) {

            throw new IllegalStateException(
                    "La IA solo puede consultar conocimiento mientras la conversacion esta en BOT");
        }

        return knowledgeRetriever.recuperar(
                input);
    }
}