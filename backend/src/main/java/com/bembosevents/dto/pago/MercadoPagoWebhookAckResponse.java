package com.bembosevents.dto.pago;

public record MercadoPagoWebhookAckResponse(
        boolean recibido,
        String estado,
        String resourceId,
        String topic,
        String action) {
}