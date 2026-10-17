package com.bembosevents.service;

import com.bembosevents.domain.EstadoPago;
import com.bembosevents.service.impl.PagoServiceImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PagoServiceImplTest {

    private final PagoService pagoService =
            new PagoServiceImpl(
                org.mockito.Mockito.mock(
                        com.bembosevents.repository.PagoRepository.class),
                org.mockito.Mockito.mock(
                        com.bembosevents.repository.ReservaRepository.class),
                org.mockito.Mockito.mock(
                        com.bembosevents.repository.ClienteRepository.class));

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