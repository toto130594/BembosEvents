package com.bembosevents.service.impl;

import com.bembosevents.dto.pago.MercadoPagoWebhookAckResponse;
import com.bembosevents.entity.EventoPagoProveedor;
import com.bembosevents.integration.mercadopago.MercadoPagoWebhookNotification;
import com.bembosevents.integration.mercadopago.MercadoPagoWebhookSignatureValidator;
import com.bembosevents.service.MercadoPagoPaymentSyncService;
import com.bembosevents.service.MercadoPagoWebhookEventStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MercadoPagoWebhookServiceImplTest {

    @Mock
    private MercadoPagoWebhookSignatureValidator signatureValidator;

    @Mock
    private MercadoPagoWebhookEventStore eventStore;

    @Mock
    private MercadoPagoPaymentSyncService paymentSyncService;

    @Test
    void eventoNuevoDebeSincronizarYMarcarProcesado() {

        MercadoPagoWebhookNotification notification =
                notification();

        when(eventStore.construirIdEventoExterno(
                notification))
                .thenReturn(
                        "id:100000000001");

        when(eventStore.buscar(
                "id:100000000001"))
                .thenReturn(
                        Optional.empty());

        when(eventStore.registrar(
                "id:100000000001",
                notification))
                .thenReturn(
                        77L);

        when(paymentSyncService.sincronizar(
                "ORD-TEST-1107"))
                .thenReturn(
                        "ESTADO_APROBADO");

        MercadoPagoWebhookServiceImpl service =
                service();

        MercadoPagoWebhookAckResponse response =
                service.recibir(
                        notification);

        assertEquals(
                "RECEIVED_VERIFIED",
                response.estado());

        verify(paymentSyncService)
                .sincronizar(
                        "ORD-TEST-1107");

        verify(eventStore)
                .marcarProcesado(
                        77L,
                        "ESTADO_APROBADO");
    }

    @Test
    void eventoProcesadoDebeIgnorarse() {

        MercadoPagoWebhookNotification notification =
                notification();

        EventoPagoProveedor existing =
                new EventoPagoProveedor();

        existing.setIdEventoPagoProveedor(
                77L);

        existing.setProcesado(
                true);

        when(eventStore.construirIdEventoExterno(
                notification))
                .thenReturn(
                        "id:100000000001");

        when(eventStore.buscar(
                "id:100000000001"))
                .thenReturn(
                        Optional.of(
                                existing));

        MercadoPagoWebhookServiceImpl service =
                service();

        MercadoPagoWebhookAckResponse response =
                service.recibir(
                        notification);

        assertEquals(
                "DUPLICATE_IGNORED",
                response.estado());

        verify(paymentSyncService, never())
                .sincronizar(
                        org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void eventoNoProcesadoDebeReintentarSync() {

        MercadoPagoWebhookNotification notification =
                notification();

        EventoPagoProveedor existing =
                new EventoPagoProveedor();

        existing.setIdEventoPagoProveedor(
                77L);

        existing.setProcesado(
                false);

        when(eventStore.construirIdEventoExterno(
                notification))
                .thenReturn(
                        "id:100000000001");

        when(eventStore.buscar(
                "id:100000000001"))
                .thenReturn(
                        Optional.of(
                                existing));

        when(paymentSyncService.sincronizar(
                "ORD-TEST-1107"))
                .thenReturn(
                        "SIN_CAMBIO");

        MercadoPagoWebhookServiceImpl service =
                service();

        MercadoPagoWebhookAckResponse response =
                service.recibir(
                        notification);

        assertEquals(
                "RECEIVED_VERIFIED",
                response.estado());

        verify(eventStore)
                .marcarProcesado(
                        77L,
                        "SIN_CAMBIO");
    }

    private MercadoPagoWebhookServiceImpl service() {

        return new MercadoPagoWebhookServiceImpl(
                signatureValidator,
                eventStore,
                paymentSyncService);
    }

    private MercadoPagoWebhookNotification notification() {

        return new MercadoPagoWebhookNotification(
                "100000000001",
                "ORD-TEST-1107",
                "order",
                "order.processed",
                "req-test-1107",
                "ts=1,v1=fake",
                true,
                false,
                "2026-10-21T21:30:00Z");
    }
}