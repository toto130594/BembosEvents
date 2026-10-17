package com.bembosevents.service.impl;

import com.bembosevents.dto.pago.PagoResponse;
import com.bembosevents.entity.Cliente;
import com.bembosevents.entity.Pago;
import com.bembosevents.entity.Reserva;
import com.bembosevents.exception.RecursoNoEncontradoException;
import com.bembosevents.repository.ClienteRepository;
import com.bembosevents.repository.PagoRepository;
import com.bembosevents.repository.ReservaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PagoConsultaClienteServiceTest {

    @Mock
    private PagoRepository pagoRepository;

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private ClienteRepository clienteRepository;

    private PagoServiceImpl pagoService;

    @BeforeEach
    void setUp() {

        pagoService =
                new PagoServiceImpl(
                        pagoRepository,
                        reservaRepository,
                        clienteRepository);
    }

    @Test
    void obtenerPagoPropioRetornaDetalle() {

        Cliente cliente =
                new Cliente();

        cliente.setIdCliente(10);

        Reserva reserva =
                new Reserva();

        reserva.setIdReserva(20);
        reserva.setCliente(cliente);

        Pago pago =
                new Pago();

        pago.setIdPago(30);
        pago.setReserva(reserva);
        pago.setMonto(
                new BigDecimal("150.00"));
        pago.setMetodoPago("PRUEBA");
        pago.setEstadoPago("PENDIENTE");
        pago.setFechaCreacion(
                LocalDateTime.of(
                        2026,
                        10,
                        17,
                        1,
                        0));

        when(
                clienteRepository
                        .findByUsuario_CorreoIgnoreCaseAndActivoTrue(
                                "cliente@bembos.test")
        ).thenReturn(
                Optional.of(cliente));

        when(
                pagoRepository
                        .findByIdPagoAndReserva_Cliente_IdCliente(
                                30,
                                10)
        ).thenReturn(
                Optional.of(pago));

        PagoResponse response =
                pagoService.obtenerPagoPropio(
                        "cliente@bembos.test",
                        30);

        assertEquals(
                30,
                response.idPago());

        assertEquals(
                20,
                response.idReserva());

        assertEquals(
                new BigDecimal("150.00"),
                response.monto());

        assertEquals(
                "PENDIENTE",
                response.estadoPago());

        verify(
                pagoRepository)
                .findByIdPagoAndReserva_Cliente_IdCliente(
                        30,
                        10);
    }

    @Test
    void obtenerPagoAjenoONoExistenteResponde404() {

        Cliente cliente =
                new Cliente();

        cliente.setIdCliente(10);

        when(
                clienteRepository
                        .findByUsuario_CorreoIgnoreCaseAndActivoTrue(
                                "cliente@bembos.test")
        ).thenReturn(
                Optional.of(cliente));

        when(
                pagoRepository
                        .findByIdPagoAndReserva_Cliente_IdCliente(
                                99,
                                10)
        ).thenReturn(
                Optional.empty());

        RecursoNoEncontradoException exception =
                assertThrows(
                        RecursoNoEncontradoException.class,
                        () ->
                                pagoService.obtenerPagoPropio(
                                        "cliente@bembos.test",
                                        99));

        assertEquals(
                "Pago no encontrado",
                exception.getMessage());
    }

    @Test
    void obtenerPagoIdInvalidoEsRechazado() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                pagoService.obtenerPagoPropio(
                                        "cliente@bembos.test",
                                        0));

        assertEquals(
                "El id de pago debe ser positivo",
                exception.getMessage());
    }
}