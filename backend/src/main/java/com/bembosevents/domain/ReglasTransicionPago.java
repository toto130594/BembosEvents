package com.bembosevents.domain;

public final class ReglasTransicionPago {

    private ReglasTransicionPago() {
    }

    public static boolean esValida(EstadoPago estadoActual, EstadoPago nuevoEstado) {
        if (estadoActual == null || nuevoEstado == null) {
            return false;
        }

        return switch (estadoActual) {
            case PENDIENTE ->
                    nuevoEstado == EstadoPago.APROBADO
                    || nuevoEstado == EstadoPago.RECHAZADO
                    || nuevoEstado == EstadoPago.CANCELADO;

            case APROBADO ->
                    nuevoEstado == EstadoPago.REEMBOLSADO;

            case RECHAZADO, CANCELADO, REEMBOLSADO -> false;
        };
    }
}
