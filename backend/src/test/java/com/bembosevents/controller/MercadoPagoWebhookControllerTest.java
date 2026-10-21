package com.bembosevents.controller;

import com.bembosevents.dto.pago.MercadoPagoWebhookAckResponse;
import com.bembosevents.integration.mercadopago.MercadoPagoWebhookNotification;
import com.bembosevents.service.MercadoPagoWebhookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MercadoPagoWebhookControllerTest {

    @Mock
    private MercadoPagoWebhookService webhookService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {

        mockMvc =
                MockMvcBuilders
                        .standaloneSetup(
                                new MercadoPagoWebhookController(
                                        webhookService))
                        .build();
    }

    @Test
    void debeRecibirOrderWebhookConMetadatos() throws Exception {

        when(webhookService.recibir(any()))
                .thenReturn(
                        new MercadoPagoWebhookAckResponse(
                                true,
                                "RECEIVED_VERIFIED",
                                "ORD-TEST-1104",
                                "order",
                                "order.processed"));

        mockMvc.perform(
                        post(
                                "/api/webhooks/pagos/mercado-pago"
                                        + "?data.id=ORD-TEST-1104"
                                        + "&type=order")
                                .header(
                                        "x-request-id",
                                        "req-test-1104")
                                .header(
                                        "x-signature",
                                        "ts=1,v1=fake")
                                .contentType(
                                        MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "id": "100000000001",
                                          "action": "order.processed",
                                          "type": "order",
                                          "live_mode": false,
                                          "date_created": "2026-10-21T20:00:00Z",
                                          "data": {
                                            "id": "ORD-BODY-FALLBACK"
                                          }
                                        }
                                        """))
                .andExpect(
                        status().isOk())
                .andExpect(
                        jsonPath("$.recibido")
                                .value(true))
                .andExpect(
                        jsonPath("$.estado")
                                .value("RECEIVED_VERIFIED"));

        ArgumentCaptor<MercadoPagoWebhookNotification> captor =
                ArgumentCaptor.forClass(
                        MercadoPagoWebhookNotification.class);

        verify(webhookService)
                .recibir(
                        captor.capture());

        MercadoPagoWebhookNotification received =
                captor.getValue();

        assertEquals(
                "ORD-TEST-1104",
                received.resourceId());

        assertEquals(
                "order",
                received.topic());

        assertEquals(
                "order.processed",
                received.action());

        assertEquals(
                "req-test-1104",
                received.requestId());

        assertTrue(
                received.signaturePresent());
    }

    @Test
    void debeUsarBodyComoFallback() throws Exception {

        when(webhookService.recibir(any()))
                .thenReturn(
                        new MercadoPagoWebhookAckResponse(
                                true,
                                "RECEIVED_VERIFIED",
                                "ORD-BODY",
                                "order",
                                "order.created"));

        mockMvc.perform(
                        post(
                                "/api/webhooks/pagos/mercado-pago")
                                .contentType(
                                        MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "action": "order.created",
                                          "type": "order",
                                          "live_mode": false,
                                          "date_created": "2026-10-21T20:00:00Z",
                                          "data": {
                                            "id": "ORD-BODY"
                                          }
                                        }
                                        """))
                .andExpect(
                        status().isOk());

        ArgumentCaptor<MercadoPagoWebhookNotification> captor =
                ArgumentCaptor.forClass(
                        MercadoPagoWebhookNotification.class);

        verify(webhookService)
                .recibir(
                        captor.capture());

        assertEquals(
                "ORD-BODY",
                captor.getValue().resourceId());

        assertEquals(
                "order",
                captor.getValue().topic());
    }
}