package com.bembosevents.service;

import com.bembosevents.dto.pago.MercadoPagoWebhookAckResponse;
import com.bembosevents.integration.mercadopago.MercadoPagoWebhookNotification;

public interface MercadoPagoWebhookService {

    MercadoPagoWebhookAckResponse recibir(
            MercadoPagoWebhookNotification notification);
}