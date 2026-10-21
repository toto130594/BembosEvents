package com.bembosevents.service;

import com.bembosevents.domain.EstadoPago;
import com.bembosevents.dto.pago.PagoRefundResponse;
import com.bembosevents.dto.pago.PagoResponse;
import com.bembosevents.entity.Pago;
import com.bembosevents.exception.RecursoNoEncontradoException;
import com.bembosevents.integration.mercadopago.MercadoPagoOrderResult;
import com.bembosevents.integration.mercadopago.MercadoPagoOrderStatusMapper;
import com.bembosevents.integration.mercadopago.MercadoPagoProvider;
import com.bembosevents.repository.PagoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
public class MercadoPagoRefundService {

    private static final Logger log =
            LoggerFactory.getLogger(
                    MercadoPagoRefundService.class);

    private static final String METODO =
            "MERCADO_PAGO";

    private final PagoRepository pagoRepository;
    private final MercadoPagoProvider mercadoPagoProvider;
    private final MercadoPagoOrderStatusMapper statusMapper;
    private final PagoService pagoService;

    public MercadoPagoRefundService(
            PagoRepository pagoRepository,
            MercadoPagoProvider mercadoPagoProvider,
            MercadoPagoOrderStatusMapper statusMapper,
            PagoService pagoService) {

        this.pagoRepository =
                pagoRepository;

        this.mercadoPagoProvider =
                mercadoPagoProvider;

        this.statusMapper =
                statusMapper;

        this.pagoService =
                pagoService;
    }

    public PagoRefundResponse reembolsarTotal(
            Integer idPago) {

        if (idPago == null
                || idPago <= 0) {

            throw new IllegalArgumentException(
                    "El id de pago debe ser positivo");
        }

        Pago pago =
                pagoRepository
                        .findById(
                                idPago)
                        .orElseThrow(() ->
                                new RecursoNoEncontradoException(
                                        "Pago no encontrado"));

        validarMercadoPago(
                pago);

        EstadoPago estadoActual =
                EstadoPago.valueOf(
                        pago.getEstadoPago());

        if (estadoActual == EstadoPago.REEMBOLSADO) {

            log.info(
                    "MP_REFUND_ALREADY_LOCAL paymentId={} orderId={}",
                    pago.getIdPago(),
                    pago.getReferenciaExterna());

            return response(
                    convertir(
                            pago),
                    "refunded",
                    "refunded",
                    false);
        }

        if (estadoActual != EstadoPago.APROBADO) {
            throw new IllegalStateException(
                    "Solo un pago APROBADO puede reembolsarse");
        }

        String orderId =
                pago.getReferenciaExterna()
                        .trim();

        MercadoPagoOrderResult currentOrder =
                mercadoPagoProvider
                        .consultarOrden(
                                orderId);

        validarOrderId(
                orderId,
                currentOrder);

        EstadoPago providerCurrentState =
                statusMapper
                        .map(
                                currentOrder)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "La order de Mercado Pago no esta en un estado reembolsable"));

        if (providerCurrentState == EstadoPago.REEMBOLSADO) {

            PagoResponse synchronizedPago =
                    pagoService
                            .actualizarEstadoDesdeProveedor(
                                    orderId,
                                    EstadoPago.REEMBOLSADO);

            log.info(
                    "MP_REFUND_PROVIDER_ALREADY_REFUNDED paymentId={} orderId={}",
                    idPago,
                    orderId);

            return response(
                    synchronizedPago,
                    currentOrder.status(),
                    currentOrder.statusDetail(),
                    false);
        }

        if (providerCurrentState != EstadoPago.APROBADO) {
            throw new IllegalStateException(
                    "La order de Mercado Pago no esta acreditada");
        }

        String idempotencyKey =
                refundIdempotencyKey(
                        pago);

        MercadoPagoOrderResult refundOrder =
                mercadoPagoProvider
                        .reembolsarOrdenTotal(
                                orderId,
                                idempotencyKey);

        validarOrderId(
                orderId,
                refundOrder);

        EstadoPago refundState =
                statusMapper
                        .map(
                                refundOrder)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Mercado Pago no confirmo el reembolso"));

        if (refundState != EstadoPago.REEMBOLSADO) {
            throw new IllegalStateException(
                    "Mercado Pago no confirmo estado REEMBOLSADO");
        }

        PagoResponse updatedPago =
                pagoService
                        .actualizarEstadoDesdeProveedor(
                                orderId,
                                EstadoPago.REEMBOLSADO);

        log.info(
                "MP_REFUND_COMPLETED_LOCAL paymentId={} orderId={}",
                idPago,
                orderId);

        return response(
                updatedPago,
                refundOrder.status(),
                refundOrder.statusDetail(),
                true);
    }

    private void validarMercadoPago(
            Pago pago) {

        if (pago.getMetodoPago() == null
                || !METODO.equalsIgnoreCase(
                        pago.getMetodoPago())) {

            throw new IllegalStateException(
                    "El pago no pertenece a Mercado Pago");
        }

        if (pago.getReferenciaExterna() == null
                || pago.getReferenciaExterna().isBlank()) {

            throw new IllegalStateException(
                    "El pago no tiene orderId de Mercado Pago");
        }
    }

    private void validarOrderId(
            String expectedOrderId,
            MercadoPagoOrderResult order) {

        if (order == null) {
            throw new IllegalStateException(
                    "Mercado Pago devolvio una order vacia");
        }

        if (order.id() == null
                || !expectedOrderId.equals(
                        order.id())) {

            throw new IllegalStateException(
                    "Mercado Pago devolvio una order diferente");
        }
    }

    private String refundIdempotencyKey(
            Pago pago) {

        String canonical =
                "BEMBOS-MP-TOTAL-REFUND:"
                        + pago.getIdPago()
                        + ":"
                        + pago.getReferenciaExterna()
                                .trim();

        return UUID
                .nameUUIDFromBytes(
                        canonical.getBytes(
                                StandardCharsets.UTF_8))
                .toString();
    }

    private PagoRefundResponse response(
            PagoResponse pago,
            String providerStatus,
            String providerStatusDetail,
            boolean providerCallExecuted) {

        return new PagoRefundResponse(
                pago,
                "MERCADO_PAGO",
                pago.referenciaExterna(),
                providerStatus,
                providerStatusDetail,
                providerCallExecuted);
    }

    private PagoResponse convertir(
            Pago pago) {

        return new PagoResponse(
                pago.getIdPago(),
                pago.getReserva()
                        .getIdReserva(),
                pago.getMonto(),
                pago.getMetodoPago(),
                pago.getEstadoPago(),
                pago.getReferenciaExterna(),
                pago.getFechaPago(),
                pago.getFechaCreacion());
    }
}