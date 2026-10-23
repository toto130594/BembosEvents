package com.bembosevents.ai.knowledge;

import java.util.List;

public record KnowledgeRetrievalResult(
        String pregunta,
        List<KnowledgeHit> evidencias) {

    public KnowledgeRetrievalResult {
        if (pregunta == null
                || pregunta.isBlank()) {
            throw new IllegalArgumentException(
                    "La pregunta es obligatoria");
        }

        evidencias =
                evidencias == null
                        ? List.of()
                        : List.copyOf(evidencias);
    }

    public boolean tieneEvidencia() {
        return !evidencias.isEmpty();
    }
}