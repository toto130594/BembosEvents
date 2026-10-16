package com.bembosevents.domain;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReglasTransicionPagoTest {

    private static final Set<String> TRANSICIONES_VALIDAS = Set.of(
            "PENDIENTE->APROBADO",
            "PENDIENTE->RECHAZADO",
            "PENDIENTE->CANCELADO",
            "APROBADO->REEMBOLSADO"
    );

    @Test
    void debeAceptarTodasLasTransicionesDocumentadas() {
        for (String transicion : TRANSICIONES_VALIDAS) {
            String[] partes = transicion.split("->");

            EstadoPago actual = EstadoPago.valueOf(partes[0]);
            EstadoPago nuevo = EstadoPago.valueOf(partes[1]);

            assertTrue(
                    ReglasTransicionPago.esValida(actual, nuevo),
                    "Deberia ser valida: " + transicion
            );
        }
    }

    @Test
    void debeRechazarTodasLasTransicionesNoDocumentadas() {
        for (EstadoPago actual : EstadoPago.values()) {
            for (EstadoPago nuevo : EstadoPago.values()) {
                String transicion = actual.name() + "->" + nuevo.name();

                if (!TRANSICIONES_VALIDAS.contains(transicion)) {
                    assertFalse(
                            ReglasTransicionPago.esValida(actual, nuevo),
                            "Deberia ser invalida: " + transicion
                    );
                }
            }
        }
    }

    @Test
    void debeRechazarEstadosNulos() {
        assertFalse(ReglasTransicionPago.esValida(null, EstadoPago.PENDIENTE));
        assertFalse(ReglasTransicionPago.esValida(EstadoPago.PENDIENTE, null));
        assertFalse(ReglasTransicionPago.esValida(null, null));
    }
}
