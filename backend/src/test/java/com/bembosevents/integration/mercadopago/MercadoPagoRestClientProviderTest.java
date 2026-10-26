package com.bembosevents.integration.mercadopago;

import com.bembosevents.config.MercadoPagoProperties;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class MercadoPagoRestClientProviderTest {

    @Test
    void debeCrearOrderCheckoutProConIdempotencia() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        MercadoPagoRestClientProvider provider =
                new MercadoPagoRestClientProvider(builder,propertiesHabilitadas());

        server.expect(requestTo("https://api.mercadopago.com/v1/orders"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION,"Bearer TEST_ACCESS_TOKEN"))
                .andExpect(header(
                        "X-Idempotency-Key",
                        "11111111-2222-3333-4444-555555555555"))
                .andExpect(content().json(
                        """
                        {
                          "type": "online",
                          "processing_mode": "manual",
                          "total_amount": "150.00",
                          "external_reference": "PAGO-25",
                          "payer": {"email": "buyer@testuser.com"},
                          "items": [{
                            "title": "Reserva Bembos Events",
                            "quantity": 1,
                            "unit_price": "150.00"
                          }]
                        }
                        """))
                .andRespond(withSuccess(
                        """
                        {
                          "id": "ORD-123",
                          "status": "created",
                          "checkout_url": "https://www.mercadopago.com/checkout/v1/redirect?pref_id=test",
                          "external_reference": "PAGO-25"
                        }
                        """,
                        MediaType.APPLICATION_JSON));

        MercadoPagoOrderResult result = provider.crearOrden(
                new MercadoPagoOrderRequest(
                        new BigDecimal("150.00"),
                        "PAGO-25",
                        "buyer@testuser.com",
                        "Reserva Bembos Events"),
                "11111111-2222-3333-4444-555555555555");

        assertEquals("ORD-123",result.id());
        assertEquals("created",result.status());
        assertEquals("PAGO-25",result.externalReference());
        server.verify();
    }

    @Test
    void debeConsultarOrderPorId() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        MercadoPagoRestClientProvider provider =
                new MercadoPagoRestClientProvider(builder,propertiesHabilitadas());

        server.expect(requestTo("https://api.mercadopago.com/v1/orders/ORD-123"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION,"Bearer TEST_ACCESS_TOKEN"))
                .andRespond(withSuccess(
                        """
                        {
                          "id": "ORD-123",
                          "status": "processed",
                          "external_reference": "PAGO-25"
                        }
                        """,
                        MediaType.APPLICATION_JSON));

        MercadoPagoOrderResult result = provider.consultarOrden("ORD-123");
        assertEquals("ORD-123",result.id());
        assertEquals("processed",result.status());
        server.verify();
    }

    @Test
    void debeFallarCerradoSiEstaDeshabilitado() {
        RestClient.Builder builder = RestClient.builder();
        MercadoPagoProperties properties = new MercadoPagoProperties();
        properties.setEnabled(false);
        properties.setAccessToken("TEST_ACCESS_TOKEN");
        MercadoPagoRestClientProvider provider =
                new MercadoPagoRestClientProvider(builder,properties);

        assertThrows(
                IllegalStateException.class,
                () -> provider.crearOrden(
                        new MercadoPagoOrderRequest(
                                new BigDecimal("100.00"),
                                "PAGO-1",
                                "buyer@testuser.com",
                                "Reserva Bembos Events"),
                        "idem-1"));
    }

    @Test
    void debeFallarCerradoSinAccessToken() {
        RestClient.Builder builder = RestClient.builder();
        MercadoPagoProperties properties = new MercadoPagoProperties();
        properties.setEnabled(true);
        properties.setAccessToken("");
        MercadoPagoRestClientProvider provider =
                new MercadoPagoRestClientProvider(builder,properties);

        assertThrows(
                IllegalStateException.class,
                () -> provider.consultarOrden("ORD-123"));
    }

    private MercadoPagoProperties propertiesHabilitadas() {
        MercadoPagoProperties properties = new MercadoPagoProperties();
        properties.setEnabled(true);
        properties.setBaseUrl("https://api.mercadopago.com");
        properties.setAccessToken("TEST_ACCESS_TOKEN");
        return properties;
    }
}