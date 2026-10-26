package com.bembosevents.integration.mercadopago;

import com.bembosevents.config.MercadoPagoProperties;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.ResourceAccessException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class MercadoPagoRestClientProvider implements MercadoPagoProvider {

    private static final Logger log =
            LoggerFactory.getLogger(MercadoPagoRestClientProvider.class);

    private final MercadoPagoProperties properties;
    private final RestClient restClient;

    public MercadoPagoRestClientProvider(
            RestClient.Builder builder,
            MercadoPagoProperties properties) {

        this.properties = Objects.requireNonNull(properties);
        this.restClient = builder.baseUrl(properties.getBaseUrl()).build();
    }

    @Override
    public MercadoPagoOrderResult crearOrden(
            MercadoPagoOrderRequest request,
            String idempotencyKey) {

        validarConfiguracion();
        Objects.requireNonNull(request,"request es obligatorio");
        validarIdempotencyKey(idempotencyKey);

        if (request.totalAmount() == null
                || request.totalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "totalAmount debe ser mayor que cero");
        }

        String totalAmount = request.totalAmount()
                .setScale(2,RoundingMode.HALF_UP)
                .toPlainString();

        String title = request.title() == null || request.title().isBlank()
                ? "Reserva Bembos Events"
                : request.title().trim();

        Map<String,Object> item = new LinkedHashMap<>();
        item.put("title",title);
        item.put("quantity",1);
        item.put("unit_price",totalAmount);
Map<String,Object> body = new LinkedHashMap<>();
        body.put("type","online");
        body.put("processing_mode","manual");
        body.put("total_amount",totalAmount);

        if (request.externalReference() != null
                && !request.externalReference().isBlank()) {
            body.put("external_reference",request.externalReference().trim());
        }

        if (request.payerEmail() != null
                && !request.payerEmail().isBlank()) {
            body.put("payer",Map.of("email",request.payerEmail().trim()));
        }

        if (properties.hasReturnBaseUrl()
                && request.externalReference() != null
                && !request.externalReference().isBlank()) {

            String returnBaseUrl =
                    properties
                            .getReturnBaseUrl()
                            .trim();

            while (returnBaseUrl.endsWith("/")) {
                returnBaseUrl =
                        returnBaseUrl.substring(
                                0,
                                returnBaseUrl.length() - 1);
            }

            String encodedReference =
                    URLEncoder.encode(
                            request.externalReference().trim(),
                            StandardCharsets.UTF_8);

            String resultUrl =
                    returnBaseUrl
                            + "/pagos/mercado-pago/resultado?pago="
                            + encodedReference;

            Map<String,Object> online =
                    new LinkedHashMap<>();

            online.put(
                    "success_url",
                    resultUrl + "&resultado=success");

            online.put(
                    "failure_url",
                    resultUrl + "&resultado=failure");

            online.put(
                    "pending_url",
                    resultUrl + "&resultado=pending");

            online.put(
                    "auto_return",
                    "all");

            body.put(
                    "config",
                    Map.of(
                            "online",
                            online));

            log.info(
                    "MP_RETURN_URLS_CONFIGURED externalReference={}",
                    request.externalReference());
        }

        body.put("items",List.of(item));

        log.info(
                "MP_ORDER_CREATE_STARTED externalReference={} idempotencyKey={}",
                request.externalReference(),
                idempotencyKey);

        JsonNode response;

        try {

            response = restClient.post()
                    .uri("/v1/orders")
                    .header(
                            HttpHeaders.AUTHORIZATION,
                            "Bearer " + properties.getAccessToken())
                    .header(
                            "X-Idempotency-Key",
                            idempotencyKey)
                    .contentType(
                            MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);

        }
        catch (RestClientResponseException ex) {

            log.error(
                    "MP_ORDER_CREATE_HTTP_ERROR status={} responseBody={}",
                    ex.getStatusCode(),
                    safeResponseBody(
                            ex.getResponseBodyAsString()));

            throw ex;
        }
        catch (ResourceAccessException ex) {

            log.error(
                    "MP_ORDER_CREATE_CONNECTION_ERROR type={} message={}",
                    ex.getClass().getSimpleName(),
                    ex.getMessage());

            throw ex;
        }

        MercadoPagoOrderResult result = convertir(response);

        log.info(
                "MP_ORDER_CREATED orderId={} status={} externalReference={}",
                result.id(),
                result.status(),
                result.externalReference());

        return result;
    }

    @Override
    public MercadoPagoOrderResult consultarOrden(String orderId) {

        validarConfiguracion();

        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("orderId es obligatorio");
        }

        log.info("MP_ORDER_QUERY_STARTED orderId={}",orderId);

        JsonNode response = restClient.get()
                .uri("/v1/orders/{orderId}",orderId)
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + properties.getAccessToken())
                .retrieve()
                .body(JsonNode.class);

        MercadoPagoOrderResult result = convertir(response);

        log.info(
                "MP_ORDER_QUERY_COMPLETED orderId={} status={}",
                result.id(),
                result.status());

        return result;
    }

    @Override
    public MercadoPagoOrderResult reembolsarOrdenTotal(
            String orderId,
            String idempotencyKey) {

        validarConfiguracion();

        if (orderId == null
                || orderId.isBlank()) {

            throw new IllegalArgumentException(
                    "orderId es obligatorio");
        }

        validarIdempotencyKey(
                idempotencyKey);

        log.info(
                "MP_REFUND_REQUESTED orderId={}",
                orderId);

        JsonNode response =
                restClient
                        .post()
                        .uri(
                                "/v1/orders/{orderId}/refund",
                                orderId.trim())
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer "
                                        + properties
                                                .getAccessToken())
                        .header(
                                "X-Idempotency-Key",
                                idempotencyKey)
                        .contentType(
                                MediaType.APPLICATION_JSON)
                        .body(
                                Map.of())
                        .retrieve()
                        .body(
                                JsonNode.class);

        MercadoPagoOrderResult result =
                convertir(
                        response);

        log.info(
                "MP_REFUND_COMPLETED orderId={} status={} statusDetail={}",
                result.id(),
                result.status(),
                result.statusDetail());

        return result;
    }
    private void validarConfiguracion() {
        if (!properties.isEnabled()) {
            throw new IllegalStateException(
                    "Mercado Pago TEST esta deshabilitado");
        }
        if (!properties.hasAccessToken()) {
            throw new IllegalStateException(
                    "MERCADOPAGO_ACCESS_TOKEN_TEST no configurado");
        }
    }

    private void validarIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException(
                    "X-Idempotency-Key es obligatorio");
        }
        if (idempotencyKey.length() > 128) {
            throw new IllegalArgumentException(
                    "X-Idempotency-Key no puede superar 128 caracteres");
        }
    }

    private MercadoPagoOrderResult convertir(JsonNode response) {
        if (response == null) {
            throw new IllegalStateException(
                    "Mercado Pago devolvio una respuesta vacia");
        }
        return new MercadoPagoOrderResult(
                text(response,"id"),
                text(response,"status"),
                text(response,"status_detail"),
                text(response,"checkout_url"),
                text(response,"external_reference"));
    }

    private String text(JsonNode node,String field) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) { return null; }
        return value.asText();
    }
    private String safeResponseBody(
            String body) {

        if (body == null
                || body.isBlank()) {

            return "-";
        }

        String normalized =
                body
                        .replace('\r', ' ')
                        .replace('\n', ' ');

        if (normalized.length() > 2000) {

            return normalized.substring(
                    0,
                    2000);
        }

        return normalized;
    }
}