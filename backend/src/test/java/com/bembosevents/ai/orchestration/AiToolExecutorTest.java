package com.bembosevents.ai.orchestration;

import com.bembosevents.ai.tool.AiTool;
import com.bembosevents.ai.tool.AiToolContext;
import com.bembosevents.ai.tool.AiToolRegistry;
import com.bembosevents.ai.tool.AiToolType;
import com.bembosevents.ai.knowledge.KnowledgeQuery;
import com.bembosevents.ai.knowledge.KnowledgeRetrievalResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AiToolExecutorTest {

    @Test
    void ejecutaSoloToolAllowlistedConInputYOutputTipados() {
        KnowledgeRetrievalResult expected =
                new KnowledgeRetrievalResult(
                        "reserva",
                        List.of());

        StubKnowledgeTool tool =
                new StubKnowledgeTool(
                        expected);

        AiToolExecutor executor =
                new AiToolExecutor(
                        new AiToolRegistry(
                                List.of(
                                        tool)));

        KnowledgeQuery query =
                new KnowledgeQuery(
                        "reserva",
                        3);

        KnowledgeRetrievalResult result =
                executor.ejecutar(
                        AiToolType.CONSULTAR_CONOCIMIENTO,
                        new AiToolContext(
                                "cliente@test.com",
                                10),
                        query,
                        KnowledgeRetrievalResult.class);

        assertSame(
                expected,
                result);
    }

    @Test
    void rechazaInputDeTipoIncorrectoAntesDeEjecutarTool() {
        AiToolExecutor executor =
                new AiToolExecutor(
                        new AiToolRegistry(
                                List.of(
                                        new StubKnowledgeTool(
                                                new KnowledgeRetrievalResult(
                                                        "x",
                                                        List.of())))));

        assertThrows(
                IllegalArgumentException.class,
                () -> executor.ejecutar(
                        AiToolType.CONSULTAR_CONOCIMIENTO,
                        new AiToolContext(
                                "cliente@test.com",
                                10),
                        "input arbitrario",
                        KnowledgeRetrievalResult.class));
    }

    @Test
    void rechazaToolNoRegistrado() {
        AiToolExecutor executor =
                new AiToolExecutor(
                        new AiToolRegistry(
                                List.of()));

        assertThrows(
                IllegalArgumentException.class,
                () -> executor.ejecutar(
                        AiToolType.CONSULTAR_CONOCIMIENTO,
                        new AiToolContext(
                                "cliente@test.com",
                                10),
                        new KnowledgeQuery(
                                "reserva",
                                3),
                        KnowledgeRetrievalResult.class));
    }

    private static final class StubKnowledgeTool
            implements AiTool<KnowledgeQuery, KnowledgeRetrievalResult> {

        private final KnowledgeRetrievalResult response;

        private StubKnowledgeTool(
                KnowledgeRetrievalResult response) {

            this.response =
                    response;
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

            return response;
        }
    }
}