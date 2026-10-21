package com.bembosevents.integration.mercadopago;

public record MercadoPagoWebhookNotification(
        String eventId,
        String resourceId,
        String topic,
        String action,
        String requestId,
        String signature,
        boolean signaturePresent,
        Boolean liveMode,
        String dateCreated) {

    public MercadoPagoWebhookNotification(
            String resourceId,
            String topic,
            String action,
            String requestId,
            String signature,
            boolean signaturePresent) {

        this(
                null,
                resourceId,
                topic,
                action,
                requestId,
                signature,
                signaturePresent,
                null,
                null);
    }
}