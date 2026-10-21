package com.bembosevents.controller;

import com.bembosevents.config.MercadoPagoProperties;
import com.bembosevents.exception.GlobalExceptionHandler;
import com.bembosevents.integration.mercadopago.MercadoPagoWebhookSignatureValidator;
import com.bembosevents.service.MercadoPagoWebhookEventStore;
import com.bembosevents.service.MercadoPagoPaymentSyncService;
import com.bembosevents.service.impl.MercadoPagoWebhookServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MercadoPagoWebhookControllerSignatureTest {

    private static final String VALID_SIGNATURE =
            "ts=1742505638683,"
                    + "v1=6ed727c232f9d01244044dc85404640d"
                    + "54287b753536abca5b788e72fc493ddf";

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {

        MercadoPagoProperties properties =
                new MercadoPagoProperties();

        properties.setWebhookSecret(
                "test-webhook-secret");

        MercadoPagoWebhookSignatureValidator validator =
                new MercadoPagoWebhookSignatureValidator(
                        properties);

        MercadoPagoWebhookEventStore eventStore =
                mock(
                        MercadoPagoWebhookEventStore.class);

        when(eventStore.construirIdEventoExterno(
                any()))
                .thenReturn(
                        "id:test-signature-event");

        when(eventStore.existe(
                "id:test-signature-event"))
                .thenReturn(
                        false);

        when(eventStore.registrar(
                anyString(),
                any()))
                .thenReturn(
                        1L);

        MercadoPagoPaymentSyncService paymentSyncService =
                mock(
                        MercadoPagoPaymentSyncService.class);

        when(paymentSyncService.sincronizar(
                anyString()))
                .thenReturn(
                        "SIN_CAMBIO");

        MercadoPagoWebhookServiceImpl service =
                new MercadoPagoWebhookServiceImpl(
                        validator,
                        eventStore,
                        paymentSyncService);

        mockMvc =
                MockMvcBuilders
                        .standaloneSetup(
                                new MercadoPagoWebhookController(
                                        service))
                        .setControllerAdvice(
                                new GlobalExceptionHandler())
                        .build();
    }

    @Test
    void debeAceptarFirmaValida() throws Exception {

        mockMvc.perform(
                        post(
                                "/api/webhooks/pagos/mercado-pago"
                                        + "?data.id=ORD-TEST-1105"
                                        + "&type=order")
                                .header(
                                        "x-request-id",
                                        "req-test-1105")
                                .header(
                                        "x-signature",
                                        VALID_SIGNATURE)
                                .contentType(
                                        MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "action": "order.processed",
                                          "type": "order",
                                          "data": {
                                            "id": "ORD-TEST-1105"
                                          }
                                        }
                                        """))
                .andExpect(
                        status().isOk())
                .andExpect(
                        jsonPath("$.estado")
                                .value("RECEIVED_VERIFIED"));
    }

    @Test
    void debeRechazarFirmaInvalidaCon401() throws Exception {

        mockMvc.perform(
                        post(
                                "/api/webhooks/pagos/mercado-pago"
                                        + "?data.id=ORD-TEST-1105"
                                        + "&type=order")
                                .header(
                                        "x-request-id",
                                        "req-test-1105")
                                .header(
                                        "x-signature",
                                        "ts=1742505638683,"
                                                + "v1=aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
                                                + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
                                .contentType(
                                        MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "action": "order.processed",
                                          "type": "order",
                                          "data": {
                                            "id": "ORD-TEST-1105"
                                          }
                                        }
                                        """))
                .andExpect(
                        status().isUnauthorized());
    }

    @Test
    void debeRechazarFirmaAusenteCon401() throws Exception {

        mockMvc.perform(
                        post(
                                "/api/webhooks/pagos/mercado-pago"
                                        + "?data.id=ORD-TEST-1105"
                                        + "&type=order")
                                .header(
                                        "x-request-id",
                                        "req-test-1105")
                                .contentType(
                                        MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "action": "order.processed",
                                          "type": "order",
                                          "data": {
                                            "id": "ORD-TEST-1105"
                                          }
                                        }
                                        """))
                .andExpect(
                        status().isUnauthorized());
    }
}