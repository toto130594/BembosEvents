package com.bembosevents.dto.pago;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CrearPagoRequest(

        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(
                value = "0.01",
                message = "El monto debe ser mayor que 0")
        @Digits(
                integer = 10,
                fraction = 2,
                message = "El monto debe tener maximo 2 decimales")
        BigDecimal monto,

        @NotBlank(message = "El metodo de pago es obligatorio")
        @Size(
                max = 50,
                message = "El metodo de pago no puede superar 50 caracteres")
        String metodoPago) {
}