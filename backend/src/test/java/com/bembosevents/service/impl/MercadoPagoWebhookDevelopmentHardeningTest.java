package com.bembosevents.service.impl;

import com.bembosevents.dto.pago.MercadoPagoWebhookAckResponse;
import com.bembosevents.integration.mercadopago.MercadoPagoWebhookNotification;
import com.bembosevents.integration.mercadopago.MercadoPagoWebhookSignatureValidator;
import com.bembosevents.service.MercadoPagoPaymentSyncService;
import com.bembosevents.service.MercadoPagoWebhookEventStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class MercadoPagoWebhookDevelopmentHardeningTest {

    @Mock
    private MercadoPagoWebhookSignatureValidator signatureValidator;

    @Mock
    private MercadoPagoWebhookEventStore eventStore;

    @Mock
    private MercadoPagoPaymentSyncService paymentSyncService;

    @Test
    void debeIgnorarWebhookProductivoDespuesDeValidarFirma() {

        MercadoPagoWebhookNotification notification =
                new MercadoPagoWebhookNotification(
                        "evt-live-11011",
                        "ORD-LIVE-11011",
                        "order",
                        "order.processed",
                        "req-live-11011",
                        "ts=1,v1=fake",
                        true,
                        true,
                        "2026-10-21T22:00:00Z");

        MercadoPagoWebhookServiceImpl service =
                service();

        MercadoPagoWebhookAckResponse response =
                service.recibir(
                        notification);

        assertEquals(
                "LIVE_MODE_IGNORED",
                response.estado());

        verify(signatureValidator)
                .validar(
                        notification.signature(),
                        notification.requestId(),
                        notification.resourceId());

        verifyNoInteractions(
                eventStore,
                paymentSyncService);
    }

    @Test
    void debeIgnorarTopicDistintoDeOrderDespuesDeValidarFirma() {

        MercadoPagoWebhookNotification notification =
                new MercadoPagoWebhookNotification(
                        "evt-topic-11011",
                        "PAY-11011",
                        "payment",
                        "payment.updated",
                        "req-topic-11011",
                        "ts=1,v1=fake",
                        true,
                        false,
                        "2026-10-21T22:00:00Z");

        MercadoPagoWebhookServiceImpl service =
                service();

        MercadoPagoWebhookAckResponse response =
                service.recibir(
                        notification);

        assertEquals(
                "UNSUPPORTED_TOPIC_IGNORED",
                response.estado());

        verify(signatureValidator)
                .validar(
                        notification.signature(),
                        notification.requestId(),
                        notification.resourceId());

        verifyNoInteractions(
                eventStore,
                paymentSyncService);
    }

    private MercadoPagoWebhookServiceImpl service() {

        return new MercadoPagoWebhookServiceImpl(
                signatureValidator,
                eventStore,
                paymentSyncService);
    }
}