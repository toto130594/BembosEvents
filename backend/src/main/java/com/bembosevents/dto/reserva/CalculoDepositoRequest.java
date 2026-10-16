package com.bembosevents.dto.reserva;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CalculoDepositoRequest(

        @NotNull(message = "El monto total es obligatorio")
        @DecimalMin(
                value = "0.00",
                message = "El monto total no puede ser negativo")
        BigDecimal montoTotal) {
}