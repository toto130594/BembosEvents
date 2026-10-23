package com.bembosevents.ai.knowledge;

public record KnowledgeSource(
        String sourceId,
        String titulo,
        String sourceUri,
        String resourcePath) {

    public KnowledgeSource {
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

        if (resourcePath == null
                || !resourcePath.startsWith("knowledge/public/")) {
            throw new IllegalArgumentException(
                    "resourcePath debe pertenecer al corpus publico");
        }

        sourceId = sourceId.strip();
        titulo = titulo.strip();
        sourceUri = sourceUri.strip();
        resourcePath = resourcePath.strip();
    }
}