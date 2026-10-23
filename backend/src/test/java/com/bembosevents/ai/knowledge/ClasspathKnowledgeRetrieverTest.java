package com.bembosevents.ai.knowledge;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClasspathKnowledgeRetrieverTest {

    private final KnowledgeRetriever retriever =
            new ClasspathKnowledgeRetriever();

    @Test
    void recuperaDisponibilidadConFuenteAtribuida() {
        KnowledgeRetrievalResult result =
                retriever.recuperar(
                        new KnowledgeQuery(
                                "Como consulto disponibilidad de un salon?",
                                3));

        assertTrue(
                result.tieneEvidencia());

        assertFalse(
                result.evidencias()
                        .isEmpty());

        KnowledgeHit first =
                result.evidencias()
                        .get(0);

        assertEquals(
                "general",
                first.sourceId());

        assertTrue(
                first.sourceUri()
                        .startsWith(
                                "kb://bembos-events/general#chunk-"));

        assertTrue(
                first.fragmento()
                        .toLowerCase()
                        .contains(
                                "disponibilidad"));
    }

    @Test
    void recuperaReglasDeReservaDesdeCorpusPublico() {
        KnowledgeRetrievalResult result =
                retriever.recuperar(
                        new KnowledgeQuery(
                                "Que valida el backend antes de crear una reserva?",
                                3));

        assertTrue(
                result.tieneEvidencia());

        assertTrue(
                result.evidencias()
                        .stream()
                        .anyMatch(hit ->
                                "reservas".equals(
                                        hit.sourceId())));
    }

    @Test
    void noInventaEvidenciaParaConsultaFueraDelCorpus() {
        KnowledgeRetrievalResult result =
                retriever.recuperar(
                        new KnowledgeQuery(
                                "cual es la clave ssh del servidor kubernetes",
                                3));

        assertFalse(
                result.tieneEvidencia());

        assertTrue(
                result.evidencias()
                        .isEmpty());
    }

    @Test
    void respetaMaximoDeResultados() {
        KnowledgeRetrievalResult result =
                retriever.recuperar(
                        new KnowledgeQuery(
                                "cliente reserva salon backend chat",
                                1));

        assertTrue(
                result.evidencias()
                        .size() <= 1);
    }

    @Test
    void todasLasEvidenciasTienenAtribucionKb() {
        KnowledgeRetrievalResult result =
                retriever.recuperar(
                        new KnowledgeQuery(
                                "reserva cliente salon servicios",
                                5));

        assertTrue(
                result.evidencias()
                        .stream()
                        .allMatch(hit ->
                                hit.sourceUri() != null
                                        && hit.sourceUri()
                                                .startsWith(
                                                        "kb://")));
    }
}