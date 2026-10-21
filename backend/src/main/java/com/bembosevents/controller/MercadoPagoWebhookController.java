package com.bembosevents.controller;

import com.bembosevents.dto.pago.MercadoPagoWebhookAckResponse;
import com.bembosevents.integration.mercadopago.MercadoPagoWebhookNotification;
import com.bembosevents.service.MercadoPagoWebhookService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MercadoPagoWebhookController {

    private final MercadoPagoWebhookService webhookService;

    public MercadoPagoWebhookController(
            MercadoPagoWebhookService webhookService) {

        this.webhookService =
                webhookService;
    }

    @PostMapping(
            "/api/webhooks/pagos/mercado-pago")
    public ResponseEntity<MercadoPagoWebhookAckResponse> recibir(
            @RequestParam(
                    name = "data.id",
                    required = false)
            String queryResourceId,

            @RequestParam(
                    name = "type",
                    required = false)
            String queryType,

            @RequestHeader(
                    name = "x-request-id",
                    required = false)
            String requestId,

            @RequestHeader(
                    name = "x-signature",
                    required = false)
            String signature,

            @RequestBody(required = false)
            JsonNode body) {

        String resourceId =
                firstNonBlank(
                        queryResourceId,
                        bodyText(
                                body,
                                "data",
                                "id"));

        String topic =
                firstNonBlank(
                        queryType,
                        bodyText(
                                body,
                                "type"));

        String eventId =
                bodyText(
                        body,
                        "id");

        String action =
                bodyText(
                        body,
                        "action");

        Boolean liveMode =
                bodyBoolean(
                        body,
                        "live_mode");

        String dateCreated =
                bodyText(
                        body,
                        "date_created");

        MercadoPagoWebhookAckResponse response =
                webhookService.recibir(
                        new MercadoPagoWebhookNotification(
                                eventId,
                                resourceId,
                                topic,
                                action,
                                requestId,
                                signature,
                                signature != null
                                        && !signature.isBlank(),
                                liveMode,
                                dateCreated));

        return ResponseEntity.ok(
                response);
    }

    private String bodyText(
            JsonNode body,
            String... path) {

        if (body == null) {
            return null;
        }

        JsonNode current =
                body;

        for (String part : path) {
            if (current == null) {
                return null;
            }

            current =
                    current.get(part);
        }

        if (current == null
                || current.isNull()) {

            return null;
        }

        return current.asText();
    }


    private Boolean bodyBoolean(
            JsonNode body,
            String field) {

        if (body == null) {
            return null;
        }

        JsonNode value =
                body.get(field);

        if (value == null
                || value.isNull()) {

            return null;
        }

        return value.asBoolean();
    }
    private String firstNonBlank(
            String first,
            String second) {

        if (first != null
                && !first.isBlank()) {

            return first;
        }

        return second;
    }
}