package com.bembosevents.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReglasReservaTest {

    @Test
    void debeAceptarInvitadosMenoresQueCapacidad() {
        assertTrue(ReglasReserva.cumpleCapacidad(30, 50));
    }

    @Test
    void debeAceptarInvitadosIgualesALaCapacidad() {
        assertTrue(ReglasReserva.cumpleCapacidad(50, 50));
    }

    @Test
    void debeRechazarInvitadosSuperioresALaCapacidad() {
        assertFalse(ReglasReserva.cumpleCapacidad(51, 50));
    }

    @Test
    void debeRechazarCeroInvitados() {
        assertFalse(ReglasReserva.cumpleCapacidad(0, 50));
    }

    @Test
    void debeRechazarCapacidadCero() {
        assertFalse(ReglasReserva.cumpleCapacidad(10, 0));
    }

    @Test
    void debeDetectarSolapamiento() {
        assertTrue(ReglasReserva.haySolapamiento(
                LocalDateTime.of(2026, 10, 20, 14, 0),
                LocalDateTime.of(2026, 10, 20, 18, 0),
                LocalDateTime.of(2026, 10, 20, 16, 0),
                LocalDateTime.of(2026, 10, 20, 20, 0)));
    }

    @Test
    void debeDetectarNuevaReservaDentroDeExistente() {
        assertTrue(ReglasReserva.haySolapamiento(
                LocalDateTime.of(2026, 10, 20, 14, 0),
                LocalDateTime.of(2026, 10, 20, 20, 0),
                LocalDateTime.of(2026, 10, 20, 16, 0),
                LocalDateTime.of(2026, 10, 20, 18, 0)));
    }

    @Test
    void debeDetectarExistenteDentroDeNuevaReserva() {
        assertTrue(ReglasReserva.haySolapamiento(
                LocalDateTime.of(2026, 10, 20, 16, 0),
                LocalDateTime.of(2026, 10, 20, 18, 0),
                LocalDateTime.of(2026, 10, 20, 14, 0),
                LocalDateTime.of(2026, 10, 20, 20, 0)));
    }

    @Test
    void debePermitirReservasConsecutivas() {
        assertFalse(ReglasReserva.haySolapamiento(
                LocalDateTime.of(2026, 10, 20, 14, 0),
                LocalDateTime.of(2026, 10, 20, 16, 0),
                LocalDateTime.of(2026, 10, 20, 16, 0),
                LocalDateTime.of(2026, 10, 20, 18, 0)));
    }

    @Test
    void noDebeDetectarReservasSeparadas() {
        assertFalse(ReglasReserva.haySolapamiento(
                LocalDateTime.of(2026, 10, 20, 10, 0),
                LocalDateTime.of(2026, 10, 20, 12, 0),
                LocalDateTime.of(2026, 10, 20, 14, 0),
                LocalDateTime.of(2026, 10, 20, 16, 0)));
    }

    @Test
    void solapamientoDebeRechazarNull() {
        assertFalse(ReglasReserva.haySolapamiento(
                null,
                LocalDateTime.of(2026, 10, 20, 12, 0),
                LocalDateTime.of(2026, 10, 20, 14, 0),
                LocalDateTime.of(2026, 10, 20, 16, 0)));
    }

    @Test
    void solapamientoDebeRechazarIntervaloInvalido() {
        assertFalse(ReglasReserva.haySolapamiento(
                LocalDateTime.of(2026, 10, 20, 18, 0),
                LocalDateTime.of(2026, 10, 20, 14, 0),
                LocalDateTime.of(2026, 10, 20, 15, 0),
                LocalDateTime.of(2026, 10, 20, 17, 0)));
    }

    @Test
    void debeAceptarMontoIgualAlMinimo() {
        assertTrue(ReglasReserva.cumpleMontoMinimo(
                new BigDecimal("500.00"),
                new BigDecimal("500.00")));
    }

    @Test
    void debeAceptarMontoMayorAlMinimo() {
        assertTrue(ReglasReserva.cumpleMontoMinimo(
                new BigDecimal("750.00"),
                new BigDecimal("500.00")));
    }

    @Test
    void debeRechazarMontoMenorAlMinimo() {
        assertFalse(ReglasReserva.cumpleMontoMinimo(
                new BigDecimal("499.99"),
                new BigDecimal("500.00")));
    }

    @Test
    void debeRechazarMontoMinimoConNull() {
        assertFalse(ReglasReserva.cumpleMontoMinimo(
                null,
                new BigDecimal("500.00")));
    }

    @Test
    void debeRechazarMontoNegativo() {
        assertFalse(ReglasReserva.cumpleMontoMinimo(
                new BigDecimal("-10.00"),
                new BigDecimal("500.00")));
    }

    @Test
    void debeCalcularMontoTotal() {
        BigDecimal resultado = ReglasReserva.calcularMontoTotal(
                new BigDecimal("500.00"),
                new BigDecimal("150.00"));

        assertEquals(new BigDecimal("650.00"), resultado);
    }

    @Test
    void debeRedondearMontoTotalADosDecimales() {
        BigDecimal resultado = ReglasReserva.calcularMontoTotal(
                new BigDecimal("500.005"),
                new BigDecimal("100.00"));

        assertEquals(new BigDecimal("600.01"), resultado);
    }

    @Test
    void calcularMontoTotalDebeRechazarNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ReglasReserva.calcularMontoTotal(
                        null,
                        new BigDecimal("100.00")));
    }

    @Test
    void calcularMontoTotalDebeRechazarNegativos() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ReglasReserva.calcularMontoTotal(
                        new BigDecimal("-1.00"),
                        new BigDecimal("100.00")));
    }

    @Test
    void debeCalcularDepositoDelCincuentaPorCiento() {
        BigDecimal resultado = ReglasReserva.calcularAdelanto(
                new BigDecimal("500.00"),
                new BigDecimal("0.50"));

        assertEquals(new BigDecimal("250.00"), resultado);
    }

    @Test
    void debePermitirPorcentajeDeDepositoConfigurable() {
        BigDecimal resultado = ReglasReserva.calcularAdelanto(
                new BigDecimal("1000.00"),
                new BigDecimal("0.30"));

        assertEquals(new BigDecimal("300.00"), resultado);
    }

    @Test
    void debeRedondearDepositoADosDecimales() {
        BigDecimal resultado = ReglasReserva.calcularAdelanto(
                new BigDecimal("999.99"),
                new BigDecimal("0.50"));

        assertEquals(new BigDecimal("500.00"), resultado);
    }

    @Test
    void debeRechazarPorcentajeMayorAUno() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ReglasReserva.calcularAdelanto(
                        new BigDecimal("500.00"),
                        new BigDecimal("1.50")));
    }

    @Test
    void calcularDepositoDebeRechazarNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ReglasReserva.calcularAdelanto(
                        null,
                        new BigDecimal("0.50")));
    }
}