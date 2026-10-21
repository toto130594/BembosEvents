package com.bembosevents.integration.mercadopago;

import com.bembosevents.config.MercadoPagoProperties;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class MercadoPagoRestClientRefundTest {

    @Test
    void debeEnviarReembolsoTotalConIdempotencia() {

        RestClient.Builder builder =
                RestClient.builder();

        MockRestServiceServer server =
                MockRestServiceServer
                        .bindTo(
                                builder)
                        .build();

        MercadoPagoProperties properties =
                new MercadoPagoProperties();

        properties.setEnabled(
                true);

        properties.setBaseUrl(
                "https://api.mercadopago.com");

        properties.setAccessToken(
                "TEST_ACCESS_TOKEN");

        MercadoPagoRestClientProvider provider =
                new MercadoPagoRestClientProvider(
                        builder,
                        properties);

        server.expect(
                        requestTo(
                                "https://api.mercadopago.com/v1/orders/ORD-1108/refund"))
                .andExpect(
                        method(
                                HttpMethod.POST))
                .andExpect(
                        header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer TEST_ACCESS_TOKEN"))
                .andExpect(
                        header(
                                "X-Idempotency-Key",
                                "11111111-2222-3333-4444-555555555555"))
                .andExpect(
                        content().json(
                                "{}"))
                .andRespond(
                        withSuccess(
                                """
                                {
                                  "id": "ORD-1108",
                                  "status": "refunded",
                                  "status_detail": "refunded"
                                }
                                """,
                                MediaType.APPLICATION_JSON));

        MercadoPagoOrderResult result =
                provider.reembolsarOrdenTotal(
                        "ORD-1108",
                        "11111111-2222-3333-4444-555555555555");

        assertEquals(
                "ORD-1108",
                result.id());

        assertEquals(
                "refunded",
                result.status());

        assertEquals(
                "refunded",
                result.statusDetail());

        server.verify();
    }
}