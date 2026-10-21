package com.bembosevents.dto.pago;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record IniciarPagoCheckoutRequest(

        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(
                value = "0.01",
                message = "El monto debe ser mayor que 0")
        @Digits(
                integer = 10,
                fraction = 2,
                message = "El monto debe tener maximo 2 decimales")
        BigDecimal monto) {
}