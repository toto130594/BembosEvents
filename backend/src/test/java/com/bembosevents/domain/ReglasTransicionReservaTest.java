package com.bembosevents.domain;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReglasTransicionReservaTest {

    private static final Set<String> TRANSICIONES_VALIDAS = Set.of(
            "PENDIENTE->CONFIRMADA",
            "PENDIENTE->CANCELADA",
            "PENDIENTE->EXPIRADA",
            "CONFIRMADA->EN_PROCESO",
            "CONFIRMADA->CANCELADA",
            "EN_PROCESO->COMPLETADA"
    );

    @Test
    void debeAceptarTodasLasTransicionesDocumentadas() {
        for (String transicion : TRANSICIONES_VALIDAS) {
            String[] partes = transicion.split("->");

            EstadoReserva actual = EstadoReserva.valueOf(partes[0]);
            EstadoReserva nuevo = EstadoReserva.valueOf(partes[1]);

            assertTrue(
                    ReglasTransicionReserva.esValida(actual, nuevo),
                    "Deberia ser valida: " + transicion
            );
        }
    }

    @Test
    void debeRechazarTodasLasTransicionesNoDocumentadas() {
        for (EstadoReserva actual : EstadoReserva.values()) {
            for (EstadoReserva nuevo : EstadoReserva.values()) {
                String transicion = actual.name() + "->" + nuevo.name();

                if (!TRANSICIONES_VALIDAS.contains(transicion)) {
                    assertFalse(
                            ReglasTransicionReserva.esValida(actual, nuevo),
                            "Deberia ser invalida: " + transicion
                    );
                }
            }
        }
    }

    @Test
    void debeRechazarEstadosNulos() {
        assertFalse(ReglasTransicionReserva.esValida(null, EstadoReserva.PENDIENTE));
        assertFalse(ReglasTransicionReserva.esValida(EstadoReserva.PENDIENTE, null));
        assertFalse(ReglasTransicionReserva.esValida(null, null));
    }
}
