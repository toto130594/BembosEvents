package com.bembosevents.ai.knowledge;

public record KnowledgeHit(
        String sourceId,
        String titulo,
        String sourceUri,
        String fragmento,
        int score) {

    public KnowledgeHit {
        if (sourceId == null
                || sourceId.isBlank()) {
            throw new IllegalArgumentException(
                    "sourceId es obligatorio");
        }

        if (titulo == null
                || titulo.isBlank()) {
            throw new IllegalArgumentException(
                    "titulo es obligatorio");
        }

        if (sourceUri == null
                || !sourceUri.startsWith("kb://")) {
            throw new IllegalArgumentException(
                    "sourceUri debe usar el esquema kb://");
        }

        if (fragmento == null
                || fragmento.isBlank()) {
            throw new IllegalArgumentException(
                    "fragmento es obligatorio");
        }

        if (score <= 0) {
            throw new IllegalArgumentException(
                    "score debe ser mayor que cero");
        }
    }
}