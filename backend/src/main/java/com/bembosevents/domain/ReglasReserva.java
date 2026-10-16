package com.bembosevents.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

public final class ReglasReserva {

    private ReglasReserva() {
    }

    public static boolean cumpleCapacidad(
            int numeroInvitados,
            int capacidadSalon) {

        return numeroInvitados > 0
                && capacidadSalon > 0
                && numeroInvitados <= capacidadSalon;
    }

    public static boolean haySolapamiento(
            LocalDateTime inicioExistente,
            LocalDateTime finExistente,
            LocalDateTime inicioNuevo,
            LocalDateTime finNuevo) {

        if (inicioExistente == null
                || finExistente == null
                || inicioNuevo == null
                || finNuevo == null) {
            return false;
        }

        if (!inicioExistente.isBefore(finExistente)
                || !inicioNuevo.isBefore(finNuevo)) {
            return false;
        }

        return inicioExistente.isBefore(finNuevo)
                && finExistente.isAfter(inicioNuevo);
    }

    public static boolean cumpleMontoMinimo(
            BigDecimal montoTotal,
            BigDecimal montoMinimo) {

        if (montoTotal == null || montoMinimo == null) {
            return false;
        }

        if (montoTotal.signum() < 0 || montoMinimo.signum() < 0) {
            return false;
        }

        return montoTotal.compareTo(montoMinimo) >= 0;
    }

    public static BigDecimal calcularMontoTotal(
            BigDecimal montoBase,
            BigDecimal montoServicios) {

        if (montoBase == null || montoServicios == null) {
            throw new IllegalArgumentException(
                    "Monto base y monto de servicios son obligatorios");
        }

        if (montoBase.signum() < 0 || montoServicios.signum() < 0) {
            throw new IllegalArgumentException(
                    "Los montos no pueden ser negativos");
        }

        return montoBase
                .add(montoServicios)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal calcularAdelanto(
            BigDecimal montoTotal,
            BigDecimal porcentajeAdelanto) {

        if (montoTotal == null || porcentajeAdelanto == null) {
            throw new IllegalArgumentException(
                    "Monto total y porcentaje de adelanto son obligatorios");
        }

        if (montoTotal.signum() < 0) {
            throw new IllegalArgumentException(
                    "El monto total no puede ser negativo");
        }

        if (porcentajeAdelanto.compareTo(BigDecimal.ZERO) < 0
                || porcentajeAdelanto.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException(
                    "El porcentaje de adelanto debe estar entre 0 y 1");
        }

        return montoTotal
                .multiply(porcentajeAdelanto)
                .setScale(2, RoundingMode.HALF_UP);
    }
}