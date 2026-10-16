package com.bembosevents.dto.reserva;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CalculoMontoTotalRequest(

        @NotNull(message = "El monto base es obligatorio")
        @DecimalMin(
                value = "0.00",
                message = "El monto base no puede ser negativo")
        BigDecimal montoBase,

        @NotNull(message = "El monto de servicios es obligatorio")
        @DecimalMin(
                value = "0.00",
                message = "El monto de servicios no puede ser negativo")
        BigDecimal montoServicios) {
}