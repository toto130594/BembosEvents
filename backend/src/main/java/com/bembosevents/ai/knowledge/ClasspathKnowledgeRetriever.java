package com.bembosevents.ai.knowledge;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class ClasspathKnowledgeRetriever
        implements KnowledgeRetriever {

    private static final List<KnowledgeSource> ALLOWLIST =
            List.of(
                    new KnowledgeSource(
                            "general",
                            "Informacion general",
                            "kb://bembos-events/general",
                            "knowledge/public/general.md"),
                    new KnowledgeSource(
                            "reservas",
                            "Reservas",
                            "kb://bembos-events/reservas",
                            "knowledge/public/reservas.md"),
                    new KnowledgeSource(
                            "chat",
                            "Chat y atencion",
                            "kb://bembos-events/chat",
                            "knowledge/public/chat.md")
            );

    private static final Set<String> STOP_WORDS =
            Set.of(
                    "que",
                    "como",
                    "para",
                    "por",
                    "con",
                    "una",
                    "uno",
                    "unos",
                    "unas",
                    "los",
                    "las",
                    "del",
                    "desde",
                    "sobre",
                    "puedo",
                    "quiero",
                    "necesito",
                    "bembos",
                    "events");

    private final List<Chunk> chunks;

    public ClasspathKnowledgeRetriever() {
        this.chunks =
                cargarCorpus();
    }

    @Override
    public KnowledgeRetrievalResult recuperar(
            KnowledgeQuery query) {

        if (query == null) {
            throw new IllegalArgumentException(
                    "La consulta de conocimiento es obligatoria");
        }

        List<String> queryTokens =
                tokens(
                        query.pregunta());

        if (queryTokens.isEmpty()) {
            return new KnowledgeRetrievalResult(
                    query.pregunta(),
                    List.of());
        }

        List<KnowledgeHit> hits =
                chunks
                        .stream()
                        .map(chunk ->
                                evaluar(
                                        chunk,
                                        queryTokens))
                        .filter(hit ->
                                hit != null)
                        .sorted(
                                Comparator
                                        .comparingInt(
                                                KnowledgeHit::score)
                                        .reversed()
                                        .thenComparing(
                                                KnowledgeHit::sourceId)
                                        .thenComparing(
                                                KnowledgeHit::sourceUri))
                        .limit(
                                query.maxResultados())
                        .toList();

        return new KnowledgeRetrievalResult(
                query.pregunta(),
                hits);
    }

    private KnowledgeHit evaluar(
            Chunk chunk,
            List<String> queryTokens) {

        Set<String> chunkTokens =
                new HashSet<>(
                        tokens(
                                chunk.texto()));

        Set<String> titleTokens =
                new HashSet<>(
                        tokens(
                                chunk.source().titulo()));

        int score = 0;

        for (String token : queryTokens) {
            if (chunkTokens.contains(token)) {
                score += 2;
            }

            if (titleTokens.contains(token)) {
                score += 1;
            }
        }

        if (score <= 0) {
            return null;
        }

        return new KnowledgeHit(
                chunk.source().sourceId(),
                chunk.source().titulo(),
                chunk.source().sourceUri()
                        + "#chunk-"
                        + chunk.index(),
                chunk.texto(),
                score);
    }

    private List<Chunk> cargarCorpus() {

        List<Chunk> loaded =
                new ArrayList<>();

        for (KnowledgeSource source : ALLOWLIST) {
            String content =
                    leer(
                            source.resourcePath());

            List<String> paragraphs =
                    Arrays
                            .stream(
                                    content.split(
                                            "(?:\\r?\\n){2,}"))
                            .map(String::strip)
                            .filter(paragraph ->
                                    !paragraph.isBlank())
                            .filter(paragraph ->
                                    !paragraph.startsWith("#"))
                            .toList();

            int index = 1;

            for (String paragraph : paragraphs) {
                loaded.add(
                        new Chunk(
                                source,
                                index,
                                paragraph));

                index++;
            }
        }

        return List.copyOf(
                loaded);
    }

    private String leer(
            String resourcePath) {

        ClassPathResource resource =
                new ClassPathResource(
                        resourcePath);

        try (InputStream input =
                     resource.getInputStream()) {

            return new String(
                    input.readAllBytes(),
                    StandardCharsets.UTF_8);
        }
        catch (IOException ex) {
            throw new IllegalStateException(
                    "No se pudo cargar el recurso de conocimiento: "
                            + resourcePath,
                    ex);
        }
    }

    private List<String> tokens(
            String value) {

        String normalized =
                normalizar(
                        value == null
                                ? ""
                                : value);

        if (normalized.isBlank()) {
            return List.of();
        }

        return Arrays
                .stream(
                        normalized.split(
                                "[^a-z0-9]+"))
                .map(String::strip)
                .filter(token ->
                        token.length() >= 3)
                .filter(token ->
                        !STOP_WORDS.contains(token))
                .distinct()
                .toList();
    }

    private String normalizar(
            String value) {

        String normalized =
                Normalizer.normalize(
                        value,
                        Normalizer.Form.NFD);

        return normalized
                .replaceAll(
                        "\\p{M}+",
                        "")
                .toLowerCase(
                        Locale.ROOT)
                .strip();
    }

    private record Chunk(
            KnowledgeSource source,
            int index,
            String texto) {
    }
}