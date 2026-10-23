package com.bembosevents.ai.knowledge;

public record KnowledgeQuery(
        String pregunta,
        Integer maxResultados) {

    public KnowledgeQuery {
        if (pregunta == null
                || pregunta.isBlank()) {
            throw new IllegalArgumentException(
                    "La pregunta de conocimiento es obligatoria");
        }

        pregunta = pregunta.strip();

        if (maxResultados == null) {
            maxResultados = 3;
        }

        if (maxResultados < 1
                || maxResultados > 5) {
            throw new IllegalArgumentException(
                    "maxResultados debe estar entre 1 y 5");
        }
    }
}