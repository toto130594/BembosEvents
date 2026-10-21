package com.bembosevents.service;

import com.bembosevents.domain.EstadoPago;
import com.bembosevents.domain.ReglasTransicionPago;
import com.bembosevents.dto.pago.PagoReconciliationItemResponse;
import com.bembosevents.dto.pago.PagoReconciliationResponse;
import com.bembosevents.entity.Pago;
import com.bembosevents.exception.RecursoNoEncontradoException;
import com.bembosevents.integration.mercadopago.MercadoPagoOrderResult;
import com.bembosevents.integration.mercadopago.MercadoPagoOrderStatusMapper;
import com.bembosevents.integration.mercadopago.MercadoPagoProvider;
import com.bembosevents.repository.PagoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class MercadoPagoReconciliationService {

    private static final Logger log =
            LoggerFactory.getLogger(
                    MercadoPagoReconciliationService.class);

    private static final String METODO =
            "MERCADO_PAGO";

    private static final int MAX_BATCH =
            100;

    private final PagoRepository pagoRepository;
    private final MercadoPagoProvider mercadoPagoProvider;
    private final MercadoPagoOrderStatusMapper statusMapper;
    private final PagoService pagoService;

    public MercadoPagoReconciliationService(
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

    public PagoReconciliationItemResponse reconciliarPago(
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

        validarCandidato(
                pago);

        return reconciliar(
                pago);
    }

    public PagoReconciliationResponse reconciliarLote() {

        List<Pago> candidatos =
                pagoRepository
                        .findAllByOrderByFechaCreacionDesc()
                        .stream()
                        .filter(
                                this::esCandidato)
                        .limit(
                                MAX_BATCH)
                        .toList();

        List<PagoReconciliationItemResponse> items =
                new ArrayList<>();

        for (Pago pago : candidatos) {

            try {
                items.add(
                        reconciliar(
                                pago));
            }
            catch (RuntimeException ex) {

                log.warn(
                        "MP_RECONCILIATION_ERROR paymentId={} orderId={} errorType={}",
                        pago.getIdPago(),
                        safe(
                                pago.getReferenciaExterna()),
                        ex.getClass()
                                .getSimpleName());

                items.add(
                        errorItem(
                                pago,
                                ex));
            }
        }

        return resumir(
                items);
    }

    private PagoReconciliationItemResponse reconciliar(
            Pago pago) {

        String orderId =
                pago.getReferenciaExterna()
                        .trim();

        String localBefore =
                pago.getEstadoPago();

        MercadoPagoOrderResult order =
                mercadoPagoProvider
                        .consultarOrden(
                                orderId);

        validarOrder(
                orderId,
                order);

        Optional<EstadoPago> target =
                statusMapper.map(
                        order);

        if (target.isEmpty()) {

            log.info(
                    "MP_RECONCILIATION_NO_CHANGE paymentId={} orderId={} localStatus={} providerStatus={} providerStatusDetail={}",
                    pago.getIdPago(),
                    orderId,
                    localBefore,
                    safe(order.status()),
                    safe(order.statusDetail()));

            return item(
                    pago,
                    localBefore,
                    order,
                    null,
                    "SIN_CAMBIO",
                    "El estado del proveedor no requiere transicion interna");
        }

        EstadoPago local =
                parseLocalState(
                        localBefore);

        EstadoPago desired =
                target.get();

        if (local == desired) {

            log.info(
                    "MP_RECONCILIATION_IN_SYNC paymentId={} orderId={} status={}",
                    pago.getIdPago(),
                    orderId,
                    local.name());

            return item(
                    pago,
                    localBefore,
                    order,
                    desired,
                    "EN_SINCRONIA",
                    "El estado local ya coincide con Mercado Pago");
        }

        if (!ReglasTransicionPago.esValida(
                local,
                desired)) {

            log.warn(
                    "MP_RECONCILIATION_CONFLICT paymentId={} orderId={} localStatus={} desiredStatus={}",
                    pago.getIdPago(),
                    orderId,
                    local.name(),
                    desired.name());

            return item(
                    pago,
                    localBefore,
                    order,
                    desired,
                    "CONFLICTO",
                    "La transicion requerida no esta permitida por el dominio");
        }

        pagoService
                .actualizarEstadoDesdeProveedor(
                        orderId,
                        desired);

        log.info(
                "MP_RECONCILIATION_UPDATED paymentId={} orderId={} from={} to={}",
                pago.getIdPago(),
                orderId,
                local.name(),
                desired.name());

        return item(
                pago,
                localBefore,
                order,
                desired,
                "ACTUALIZADO",
                "Estado local sincronizado con Mercado Pago");
    }

    private void validarCandidato(
            Pago pago) {

        if (!esCandidato(
                pago)) {

            throw new IllegalStateException(
                    "El pago no es reconciliable con Mercado Pago");
        }
    }

    private boolean esCandidato(
            Pago pago) {

        return pago != null
                && pago.getMetodoPago() != null
                && METODO.equalsIgnoreCase(
                        pago.getMetodoPago())
                && pago.getReferenciaExterna() != null
                && !pago.getReferenciaExterna()
                        .isBlank();
    }

    private void validarOrder(
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

        if (order.status() == null
                || order.status().isBlank()) {

            throw new IllegalStateException(
                    "Mercado Pago no devolvio status");
        }
    }

    private EstadoPago parseLocalState(
            String value) {

        if (value == null
                || value.isBlank()) {

            throw new IllegalStateException(
                    "El pago local no tiene estado");
        }

        try {
            return EstadoPago.valueOf(
                    value
                            .trim()
                            .toUpperCase(
                                    Locale.ROOT));
        }
        catch (IllegalArgumentException ex) {
            throw new IllegalStateException(
                    "Estado local de pago desconocido",
                    ex);
        }
    }

    private PagoReconciliationItemResponse item(
            Pago pago,
            String localBefore,
            MercadoPagoOrderResult order,
            EstadoPago target,
            String result,
            String message) {

        return new PagoReconciliationItemResponse(
                pago.getIdPago(),
                pago.getReferenciaExterna(),
                localBefore,
                order.status(),
                order.statusDetail(),
                target == null
                        ? null
                        : target.name(),
                result,
                message);
    }

    private PagoReconciliationItemResponse errorItem(
            Pago pago,
            RuntimeException ex) {

        return new PagoReconciliationItemResponse(
                pago.getIdPago(),
                pago.getReferenciaExterna(),
                pago.getEstadoPago(),
                null,
                null,
                null,
                "ERROR",
                ex.getClass()
                        .getSimpleName());
    }

    private PagoReconciliationResponse resumir(
            List<PagoReconciliationItemResponse> items) {

        int actualizados =
                count(
                        items,
                        "ACTUALIZADO");

        int enSincronia =
                count(
                        items,
                        "EN_SINCRONIA");

        int sinCambio =
                count(
                        items,
                        "SIN_CAMBIO");

        int conflictos =
                count(
                        items,
                        "CONFLICTO");

        int errores =
                count(
                        items,
                        "ERROR");

        return new PagoReconciliationResponse(
                items.size(),
                actualizados,
                enSincronia,
                sinCambio,
                conflictos,
                errores,
                List.copyOf(
                        items));
    }

    private int count(
            List<PagoReconciliationItemResponse> items,
            String result) {

        return (int) items
                .stream()
                .filter(
                        item ->
                                result.equals(
                                        item.resultado()))
                .count();
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