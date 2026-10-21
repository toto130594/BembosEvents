package com.bembosevents.service;

import com.bembosevents.domain.EstadoPago;
import com.bembosevents.dto.pago.PagoResponse;
import com.bembosevents.integration.mercadopago.MercadoPagoOrderResult;
import com.bembosevents.integration.mercadopago.MercadoPagoOrderStatusMapper;
import com.bembosevents.integration.mercadopago.MercadoPagoProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MercadoPagoPaymentSyncService {

    private static final Logger log =
            LoggerFactory.getLogger(
                    MercadoPagoPaymentSyncService.class);

    private final MercadoPagoProvider mercadoPagoProvider;
    private final MercadoPagoOrderStatusMapper statusMapper;
    private final PagoService pagoService;

    public MercadoPagoPaymentSyncService(
            MercadoPagoProvider mercadoPagoProvider,
            MercadoPagoOrderStatusMapper statusMapper,
            PagoService pagoService) {

        this.mercadoPagoProvider =
                mercadoPagoProvider;

        this.statusMapper =
                statusMapper;

        this.pagoService =
                pagoService;
    }

    public String sincronizar(
            String orderId) {

        if (orderId == null
                || orderId.isBlank()) {

            throw new IllegalArgumentException(
                    "orderId es obligatorio");
        }

        MercadoPagoOrderResult order =
                mercadoPagoProvider
                        .consultarOrden(
                                orderId.trim());

        validarOrder(
                orderId.trim(),
                order);

        Optional<EstadoPago> target =
                statusMapper.map(
                        order);

        if (target.isEmpty()) {

            log.info(
                    "MP_PAYMENT_STATE_NO_CHANGE orderId={} providerStatus={} providerStatusDetail={}",
                    safe(order.id()),
                    safe(order.status()),
                    safe(order.statusDetail()));

            return "SIN_CAMBIO";
        }

        PagoResponse pago =
                pagoService
                        .actualizarEstadoDesdeProveedor(
                                order.id(),
                                target.get());

        log.info(
                "MP_PAYMENT_STATE_CHANGED orderId={} paymentId={} internalStatus={} providerStatus={} providerStatusDetail={}",
                safe(order.id()),
                pago.idPago(),
                safe(pago.estadoPago()),
                safe(order.status()),
                safe(order.statusDetail()));

        return "ESTADO_"
                + pago.estadoPago();
    }

    private void validarOrder(
            String expectedOrderId,
            MercadoPagoOrderResult order) {

        if (order == null) {
            throw new IllegalStateException(
                    "Mercado Pago devolvio una order vacia");
        }

        if (order.id() == null
                || order.id().isBlank()) {

            throw new IllegalStateException(
                    "Mercado Pago no devolvio orderId");
        }

        if (!expectedOrderId.equals(
                order.id())) {

            throw new IllegalStateException(
                    "Mercado Pago devolvio una order diferente");
        }

        if (order.status() == null
                || order.status().isBlank()) {

            throw new IllegalStateException(
                    "Mercado Pago no devolvio status");
        }
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