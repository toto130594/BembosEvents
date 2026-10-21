package com.bembosevents.service;

import com.bembosevents.entity.EventoPagoProveedor;
import com.bembosevents.integration.mercadopago.MercadoPagoWebhookNotification;
import com.bembosevents.repository.EventoPagoProveedorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Optional;

@Service
public class MercadoPagoWebhookEventStore {

    private static final String PROVIDER =
            "MERCADO_PAGO";

    private final EventoPagoProveedorRepository repository;

    public MercadoPagoWebhookEventStore(
            EventoPagoProveedorRepository repository) {

        this.repository = repository;
    }

    public String construirIdEventoExterno(
            MercadoPagoWebhookNotification notification) {

        if (notification == null) {
            throw new IllegalArgumentException(
                    "La notificacion es obligatoria");
        }

        if (hasText(notification.eventId())) {
            return "id:"
                    + notification.eventId().trim();
        }

        if (!hasText(notification.resourceId())
                || !hasText(notification.dateCreated())) {

            throw new IllegalArgumentException(
                    "La notificacion requiere id o data.id + date_created para idempotencia");
        }

        String canonical =
                normalize(notification.topic())
                        + "|"
                        + normalize(notification.action())
                        + "|"
                        + normalize(notification.resourceId())
                        + "|"
                        + notification.dateCreated().trim();

        return "sha256:"
                + sha256(canonical);
    }

    @Transactional(readOnly = true)
    public boolean existe(
            String idEventoExterno) {

        validarIdEventoExterno(
                idEventoExterno);

        return repository
                .existsByProveedorAndIdEventoExterno(
                        PROVIDER,
                        idEventoExterno);
    }

    @Transactional(readOnly = true)
    public Optional<EventoPagoProveedor> buscar(
            String idEventoExterno) {

        validarIdEventoExterno(
                idEventoExterno);

        return repository
                .findByProveedorAndIdEventoExterno(
                        PROVIDER,
                        idEventoExterno);
    }

    @Transactional
    public Long registrar(
            String idEventoExterno,
            MercadoPagoWebhookNotification notification) {

        validarIdEventoExterno(
                idEventoExterno);

        if (notification == null) {
            throw new IllegalArgumentException(
                    "La notificacion es obligatoria");
        }

        if (!hasText(notification.resourceId())) {
            throw new IllegalArgumentException(
                    "data.id es obligatorio");
        }

        EventoPagoProveedor entity =
                new EventoPagoProveedor();

        entity.setProveedor(
                PROVIDER);

        entity.setIdEventoExterno(
                truncate(
                        idEventoExterno,
                        200));

        entity.setIdRecursoExterno(
                truncate(
                        notification.resourceId().trim(),
                        150));

        entity.setTipoEvento(
                nullableTrim(
                        notification.topic(),
                        80));

        entity.setAccion(
                nullableTrim(
                        notification.action(),
                        120));

        entity.setRequestId(
                nullableTrim(
                        notification.requestId(),
                        150));

        entity.setLiveMode(
                notification.liveMode());

        entity.setFechaRecepcion(
                LocalDateTime.now());

        entity.setProcesado(
                false);

        entity.setResultado(
                "RECIBIDO");

        return repository
                .saveAndFlush(entity)
                .getIdEventoPagoProveedor();
    }

    @Transactional
    public void marcarProcesado(
            Long idEvento,
            String resultado) {

        EventoPagoProveedor entity =
                buscarPorId(
                        idEvento);

        entity.setProcesado(
                true);

        entity.setFechaProcesamiento(
                LocalDateTime.now());

        entity.setResultado(
                normalizeResult(
                        resultado));

        repository.save(
                entity);
    }

    @Transactional
    public void marcarErrorRetryable(
            Long idEvento,
            String resultado) {

        EventoPagoProveedor entity =
                buscarPorId(
                        idEvento);

        entity.setProcesado(
                false);

        entity.setFechaProcesamiento(
                null);

        entity.setResultado(
                normalizeResult(
                        resultado));

        repository.save(
                entity);
    }

    private EventoPagoProveedor buscarPorId(
            Long idEvento) {

        if (idEvento == null) {
            throw new IllegalArgumentException(
                    "El id interno del evento es obligatorio");
        }

        return repository
                .findById(
                        idEvento)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Evento Mercado Pago no encontrado"));
    }

    private String normalizeResult(
            String value) {

        String result =
                hasText(value)
                        ? value.trim()
                        : "SIN_RESULTADO";

        return truncate(
                result,
                50);
    }

    private void validarIdEventoExterno(
            String idEventoExterno) {

        if (!hasText(idEventoExterno)) {
            throw new IllegalArgumentException(
                    "El id externo de evento es obligatorio");
        }

        if (idEventoExterno.length() > 200) {
            throw new IllegalArgumentException(
                    "El id externo de evento no puede superar 200 caracteres");
        }
    }

    private String normalize(
            String value) {

        if (value == null) {
            return "";
        }

        return value
                .trim()
                .toLowerCase(
                        Locale.ROOT);
    }

    private boolean hasText(
            String value) {

        return value != null
                && !value.isBlank();
    }

    private String nullableTrim(
            String value,
            int maxLength) {

        if (!hasText(value)) {
            return null;
        }

        return truncate(
                value.trim(),
                maxLength);
    }

    private String truncate(
            String value,
            int maxLength) {

        if (value.length() <= maxLength) {
            return value;
        }

        return value.substring(
                0,
                maxLength);
    }

    private String sha256(
            String value) {

        try {
            MessageDigest digest =
                    MessageDigest.getInstance(
                            "SHA-256");

            return HexFormat
                    .of()
                    .formatHex(
                            digest.digest(
                                    value.getBytes(
                                            StandardCharsets.UTF_8)));
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(
                    "SHA-256 no disponible",
                    ex);
        }
    }
}