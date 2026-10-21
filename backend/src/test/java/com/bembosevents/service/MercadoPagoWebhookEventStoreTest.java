package com.bembosevents.service;

import com.bembosevents.entity.EventoPagoProveedor;
import com.bembosevents.integration.mercadopago.MercadoPagoWebhookNotification;
import com.bembosevents.repository.EventoPagoProveedorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MercadoPagoWebhookEventStoreTest {

    @Mock
    private EventoPagoProveedorRepository repository;

    @Test
    void debePreferirNotificationIdParaIdempotencia() {

        MercadoPagoWebhookEventStore store =
                new MercadoPagoWebhookEventStore(
                        repository);

        String key =
                store.construirIdEventoExterno(
                        notification(
                                "100000000001"));

        assertEquals(
                "id:100000000001",
                key);
    }

    @Test
    void debeGenerarFingerprintEstableSinNotificationId() {

        MercadoPagoWebhookEventStore store =
                new MercadoPagoWebhookEventStore(
                        repository);

        String first =
                store.construirIdEventoExterno(
                        notification(null));

        String second =
                store.construirIdEventoExterno(
                        notification(null));

        assertTrue(
                first.startsWith("sha256:"));

        assertEquals(
                first,
                second);
    }

    @Test
    void debePersistirEventoSinFirma() {

        when(repository.saveAndFlush(
                org.mockito.ArgumentMatchers.any()))
                .thenAnswer(invocation -> {
                    EventoPagoProveedor entity =
                            invocation.getArgument(0);

                    entity.setIdEventoPagoProveedor(
                            77L);

                    return entity;
                });

        MercadoPagoWebhookEventStore store =
                new MercadoPagoWebhookEventStore(
                        repository);

        Long id =
                store.registrar(
                        "id:100000000001",
                        notification(
                                "100000000001"));

        assertEquals(
                77L,
                id);

        ArgumentCaptor<EventoPagoProveedor> captor =
                ArgumentCaptor.forClass(
                        EventoPagoProveedor.class);

        verify(repository)
                .saveAndFlush(
                        captor.capture());

        EventoPagoProveedor entity =
                captor.getValue();

        assertEquals(
                "MERCADO_PAGO",
                entity.getProveedor());

        assertEquals(
                "id:100000000001",
                entity.getIdEventoExterno());

        assertEquals(
                "ORD-TEST-1106",
                entity.getIdRecursoExterno());

        assertEquals(
                "RECIBIDO",
                entity.getResultado());

        assertEquals(
                false,
                entity.getProcesado());
    }

    private MercadoPagoWebhookNotification notification(
            String eventId) {

        return new MercadoPagoWebhookNotification(
                eventId,
                "ORD-TEST-1106",
                "order",
                "order.processed",
                "req-test-1106",
                "ts=1,v1=fake",
                true,
                false,
                "2026-10-21T20:00:00Z");
    }
}