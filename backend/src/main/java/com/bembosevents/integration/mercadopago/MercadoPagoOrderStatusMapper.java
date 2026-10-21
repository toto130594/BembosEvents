package com.bembosevents.integration.mercadopago;

import com.bembosevents.domain.EstadoPago;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Optional;

@Component
public class MercadoPagoOrderStatusMapper {

    public Optional<EstadoPago> map(
            MercadoPagoOrderResult order) {

        if (order == null) {
            throw new IllegalArgumentException(
                    "La order es obligatoria");
        }

        String status =
                normalize(
                        order.status());

        String detail =
                normalize(
                        order.statusDetail());

        if ("processed".equals(status)
                && "accredited".equals(detail)) {

            return Optional.of(
                    EstadoPago.APROBADO);
        }

        if ("failed".equals(status)) {
            return Optional.of(
                    EstadoPago.RECHAZADO);
        }

        if ("canceled".equals(status)
                || "cancelled".equals(status)) {

            return Optional.of(
                    EstadoPago.CANCELADO);
        }

        if ("refunded".equals(status)
                || ("processed".equals(status)
                    && "refunded".equals(detail))) {

            return Optional.of(
                    EstadoPago.REEMBOLSADO);
        }

        return Optional.empty();
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
}