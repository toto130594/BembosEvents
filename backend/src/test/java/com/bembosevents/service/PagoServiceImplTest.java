package com.bembosevents.service;

import com.bembosevents.domain.EstadoPago;
import com.bembosevents.service.impl.PagoServiceImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PagoServiceImplTest {

    private final PagoService pagoService =
            new PagoServiceImpl();

    @Test
    void debeAceptarTransicionDePagoValida() {

        assertDoesNotThrow(
                () -> pagoService.validarTransicionEstado(
                        EstadoPago.PENDIENTE,
                        EstadoPago.APROBADO));
    }

    @Test
    void debeRechazarTransicionDePagoInvalida() {

        assertThrows(
                IllegalArgumentException.class,
                () -> pagoService.validarTransicionEstado(
                        EstadoPago.REEMBOLSADO,
                        EstadoPago.PENDIENTE));
    }
}