package com.bembosevents.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
@ConditionalOnProperty(
        name = "app.chat.ai.provider",
        havingValue = "openai-compatible"
)
public class OpenAiCompatibleChatAiProvider
        implements ChatAiProvider {

    private static final String SYSTEM_PROMPT =
            """
            Eres el asistente virtual de Bembos Events.
            Responde de forma breve, clara y amable.
            En esta fase solo puedes responder preguntas generales.
            No inventes precios, disponibilidad, capacidad ni estados.
            No apruebes pagos ni modifiques reservas.
            No afirmes haber consultado datos internos.
            Si la consulta requiere una operacion de negocio,
            explica que esa capacidad aun no esta disponible.
            """;

    private final RestClient restClient;
    private final String apiKey;
    private final String model;
    private final double temperature;

    public OpenAiCompatibleChatAiProvider(
            @Value("${app.chat.ai.base-url}")
            String baseUrl,
            @Value("${app.chat.ai.api-key:}")
            String apiKey,
            @Value("${app.chat.ai.model}")
            String model,
            @Value("${app.chat.ai.temperature:0.2}")
            double temperature) {

        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException(
                    "app.chat.ai.base-url es obligatorio");
        }

        if (model == null || model.isBlank()) {
            throw new IllegalStateException(
                    "app.chat.ai.model es obligatorio");
        }

        this.restClient =
                RestClient.builder()
                        .baseUrl(baseUrl.strip())
                        .build();

        this.apiKey =
                apiKey == null
                        ? ""
                        : apiKey.strip();

        this.model = model.strip();
        this.temperature = temperature;
    }

    @Override
    public String responder(
            ChatAiRequest request) {

        if (request == null
                || request.mensajeCliente() == null
                || request.mensajeCliente().isBlank()) {

            throw new IllegalArgumentException(
                    "El mensaje del cliente es obligatorio");
        }

        ChatCompletionRequest payload =
                new ChatCompletionRequest(
                        model,
                        temperature,
                        List.of(
                                new Message(
                                        "system",
                                        SYSTEM_PROMPT),
                                new Message(
                                        "user",
                                        request.mensajeCliente().strip())
                        )
                );

        RestClient.RequestBodySpec requestSpec =
                restClient
                        .post()
                        .uri("/chat/completions")
                        .contentType(
                                MediaType.APPLICATION_JSON);

        if (!apiKey.isBlank()) {
            requestSpec.header(
                    HttpHeaders.AUTHORIZATION,
                    "Bearer " + apiKey
            );
        }

        ChatCompletionResponse response =
                requestSpec
                        .body(payload)
                        .retrieve()
                        .body(
                                ChatCompletionResponse.class);

        if (response == null
                || response.choices() == null
                || response.choices().isEmpty()
                || response.choices().get(0).message() == null
                || response.choices().get(0).message().content() == null
                || response.choices().get(0).message().content().isBlank()) {

            throw new IllegalStateException(
                    "El proveedor IA devolvio una respuesta vacia");
        }

        return response
                .choices()
                .get(0)
                .message()
                .content()
                .strip();
    }

    private record ChatCompletionRequest(
            String model,
            double temperature,
            List<Message> messages
    ) {
    }

    private record Message(
            String role,
            String content
    ) {
    }

    private record Choice(
            Message message
    ) {
    }

    private record ChatCompletionResponse(
            List<Choice> choices
    ) {
    }
}