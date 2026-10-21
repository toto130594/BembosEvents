package com.bembosevents.service.impl;

import com.bembosevents.dto.pago.MercadoPagoWebhookAckResponse;
import com.bembosevents.entity.EventoPagoProveedor;
import com.bembosevents.integration.mercadopago.MercadoPagoWebhookNotification;
import com.bembosevents.integration.mercadopago.MercadoPagoWebhookSignatureValidator;
import com.bembosevents.service.MercadoPagoPaymentSyncService;
import com.bembosevents.service.MercadoPagoWebhookEventStore;
import com.bembosevents.service.MercadoPagoWebhookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MercadoPagoWebhookServiceImpl
        implements MercadoPagoWebhookService {

    private static final Logger log =
            LoggerFactory.getLogger(
                    MercadoPagoWebhookServiceImpl.class);

    private final MercadoPagoWebhookSignatureValidator
            signatureValidator;

    private final MercadoPagoWebhookEventStore
            eventStore;

    private final MercadoPagoPaymentSyncService
            paymentSyncService;

    public MercadoPagoWebhookServiceImpl(
            MercadoPagoWebhookSignatureValidator signatureValidator,
            MercadoPagoWebhookEventStore eventStore,
            MercadoPagoPaymentSyncService paymentSyncService) {

        this.signatureValidator =
                signatureValidator;

        this.eventStore =
                eventStore;

        this.paymentSyncService =
                paymentSyncService;
    }

    @Override
    public MercadoPagoWebhookAckResponse recibir(
            MercadoPagoWebhookNotification notification) {

        if (notification == null) {
            throw new IllegalArgumentException(
                    "La notificacion es obligatoria");
        }

        log.info(
                "MP_WEBHOOK_RECEIVED resourceId={} topic={} action={} requestId={} signaturePresent={}",
                safe(notification.resourceId()),
                safe(notification.topic()),
                safe(notification.action()),
                safe(notification.requestId()),
                notification.signaturePresent());

        try {
            signatureValidator.validar(
                    notification.signature(),
                    notification.requestId(),
                    notification.resourceId());
        }
        catch (BadCredentialsException ex) {

            log.warn(
                    "MP_WEBHOOK_SIGNATURE_INVALID resourceId={} requestId={}",
                    safe(notification.resourceId()),
                    safe(notification.requestId()));

            throw ex;
        }

        log.info(
                "MP_WEBHOOK_SIGNATURE_VALID resourceId={} requestId={}",
                safe(notification.resourceId()),
                safe(notification.requestId()));

        if (Boolean.TRUE.equals(
                notification.liveMode())) {

            log.warn(
                    "MP_WEBHOOK_LIVE_MODE_IGNORED resourceId={} topic={} requestId={}",
                    safe(notification.resourceId()),
                    safe(notification.topic()),
                    safe(notification.requestId()));

            return new MercadoPagoWebhookAckResponse(
                    true,
                    "LIVE_MODE_IGNORED",
                    notification.resourceId(),
                    notification.topic(),
                    notification.action());
        }

        if (notification.topic() == null
                || !"order".equalsIgnoreCase(
                        notification.topic().trim())) {

            log.warn(
                    "MP_WEBHOOK_UNSUPPORTED_TOPIC_IGNORED resourceId={} topic={} requestId={}",
                    safe(notification.resourceId()),
                    safe(notification.topic()),
                    safe(notification.requestId()));

            return new MercadoPagoWebhookAckResponse(
                    true,
                    "UNSUPPORTED_TOPIC_IGNORED",
                    notification.resourceId(),
                    notification.topic(),
                    notification.action());
        }
        String eventKey =
                eventStore
                        .construirIdEventoExterno(
                                notification);

        Optional<EventoPagoProveedor> existing =
                eventStore.buscar(
                        eventKey);

        if (existing.isPresent()
                && Boolean.TRUE.equals(
                        existing.get().getProcesado())) {

            return duplicado(
                    notification,
                    eventKey);
        }

        Long eventInternalId;

        if (existing.isPresent()) {
            eventInternalId =
                    existing
                            .get()
                            .getIdEventoPagoProveedor();

            log.info(
                    "MP_WEBHOOK_RETRY_UNPROCESSED eventInternalId={} resourceId={}",
                    eventInternalId,
                    safe(notification.resourceId()));
        }
        else {
            eventInternalId =
                    registrarNuevo(
                            eventKey,
                            notification);
        }

        try {
            String syncResult =
                    paymentSyncService
                            .sincronizar(
                                    notification.resourceId());

            eventStore
                    .marcarProcesado(
                            eventInternalId,
                            syncResult);

            return new MercadoPagoWebhookAckResponse(
                    true,
                    "RECEIVED_VERIFIED",
                    notification.resourceId(),
                    notification.topic(),
                    notification.action());
        }
        catch (RuntimeException ex) {

            eventStore
                    .marcarErrorRetryable(
                            eventInternalId,
                            "ERROR_SYNC");

            log.warn(
                    "MP_PAYMENT_SYNC_FAILED eventInternalId={} resourceId={} errorType={}",
                    eventInternalId,
                    safe(notification.resourceId()),
                    ex.getClass().getSimpleName());

            throw ex;
        }
    }

    private Long registrarNuevo(
            String eventKey,
            MercadoPagoWebhookNotification notification) {

        try {
            Long id =
                    eventStore
                            .registrar(
                                    eventKey,
                                    notification);

            log.info(
                    "MP_WEBHOOK_EVENT_PERSISTED eventInternalId={} resourceId={} eventKeyType={}",
                    id,
                    safe(notification.resourceId()),
                    keyType(eventKey));

            return id;
        }
        catch (DataIntegrityViolationException ex) {

            Optional<EventoPagoProveedor> raced =
                    eventStore.buscar(
                            eventKey);

            if (raced.isPresent()) {

                return raced
                        .get()
                        .getIdEventoPagoProveedor();
            }

            throw ex;
        }
    }

    private MercadoPagoWebhookAckResponse duplicado(
            MercadoPagoWebhookNotification notification,
            String eventKey) {

        log.info(
                "MP_EVENT_DUPLICATE resourceId={} eventKeyType={}",
                safe(notification.resourceId()),
                keyType(eventKey));

        return new MercadoPagoWebhookAckResponse(
                true,
                "DUPLICATE_IGNORED",
                notification.resourceId(),
                notification.topic(),
                notification.action());
    }

    private String keyType(
            String eventKey) {

        if (eventKey != null
                && eventKey.startsWith("id:")) {

            return "NOTIFICATION_ID";
        }

        return "SHA256_FINGERPRINT";
    }

    private String safe(
            String value) {

        if (value == null
                || value.isBlank()) {

            return "-";
        }

        String normalized =
                value
                        .replace('\r', '_')
                        .replace('\n', '_');

        if (normalized.length() > 160) {
            return normalized.substring(
                    0,
                    160);
        }

        return normalized;
    }

}