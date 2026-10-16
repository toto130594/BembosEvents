package com.bembosevents.domain;

public final class ReglasTransicionReserva {

    private ReglasTransicionReserva() {
    }

    public static boolean esValida(EstadoReserva estadoActual, EstadoReserva nuevoEstado) {
        if (estadoActual == null || nuevoEstado == null) {
            return false;
        }

        return switch (estadoActual) {
            case PENDIENTE ->
                    nuevoEstado == EstadoReserva.CONFIRMADA
                    || nuevoEstado == EstadoReserva.CANCELADA
                    || nuevoEstado == EstadoReserva.EXPIRADA;

            case CONFIRMADA ->
                    nuevoEstado == EstadoReserva.EN_PROCESO
                    || nuevoEstado == EstadoReserva.CANCELADA;

            case EN_PROCESO ->
                    nuevoEstado == EstadoReserva.COMPLETADA;

            case COMPLETADA, CANCELADA, EXPIRADA -> false;
        };
    }
}
