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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PagoListadoClienteServiceTest {

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
    void listarPagosPropiosRetornaPagosDeLaReserva() {

        Cliente cliente =
                new Cliente();

        cliente.setIdCliente(10);

        Reserva reserva =
                new Reserva();

        reserva.setIdReserva(20);
        reserva.setCliente(cliente);

        Pago reciente =
                crearPago(
                        2,
                        reserva,
                        "200.00",
                        LocalDateTime.of(
                                2026,
                                10,
                                16,
                                12,
                                0));

        Pago antiguo =
                crearPago(
                        1,
                        reserva,
                        "100.00",
                        LocalDateTime.of(
                                2026,
                                10,
                                15,
                                12,
                                0));

        when(
                clienteRepository
                        .findByUsuario_CorreoIgnoreCaseAndActivoTrue(
                                "cliente@bembos.test")
        ).thenReturn(
                Optional.of(cliente));

        when(
                reservaRepository
                        .findByIdReservaAndCliente_IdCliente(
                                20,
                                10)
        ).thenReturn(
                Optional.of(reserva));

        when(
                pagoRepository
                        .findByReserva_IdReservaOrderByFechaCreacionDesc(
                                20)
        ).thenReturn(
                List.of(
                        reciente,
                        antiguo));

        List<PagoResponse> response =
                pagoService.listarPagosPropios(
                        "cliente@bembos.test",
                        20);

        assertEquals(
                2,
                response.size());

        assertEquals(
                2,
                response.get(0).idPago());

        assertEquals(
                1,
                response.get(1).idPago());

        assertEquals(
                20,
                response.get(0).idReserva());

        verify(
                pagoRepository)
                .findByReserva_IdReservaOrderByFechaCreacionDesc(
                        20);
    }

    @Test
    void listarPagosPropiosSinPagosRetornaListaVacia() {

        Cliente cliente =
                new Cliente();

        cliente.setIdCliente(10);

        Reserva reserva =
                new Reserva();

        reserva.setIdReserva(20);
        reserva.setCliente(cliente);

        when(
                clienteRepository
                        .findByUsuario_CorreoIgnoreCaseAndActivoTrue(
                                "cliente@bembos.test")
        ).thenReturn(
                Optional.of(cliente));

        when(
                reservaRepository
                        .findByIdReservaAndCliente_IdCliente(
                                20,
                                10)
        ).thenReturn(
                Optional.of(reserva));

        when(
                pagoRepository
                        .findByReserva_IdReservaOrderByFechaCreacionDesc(
                                20)
        ).thenReturn(
                List.of());

        List<PagoResponse> response =
                pagoService.listarPagosPropios(
                        "cliente@bembos.test",
                        20);

        assertEquals(
                0,
                response.size());
    }

    @Test
    void listarPagosReservaAjenaRespondeNoEncontrada() {

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
                reservaRepository
                        .findByIdReservaAndCliente_IdCliente(
                                99,
                                10)
        ).thenReturn(
                Optional.empty());

        RecursoNoEncontradoException exception =
                assertThrows(
                        RecursoNoEncontradoException.class,
                        () ->
                                pagoService.listarPagosPropios(
                                        "cliente@bembos.test",
                                        99));

        assertEquals(
                "Reserva no encontrada",
                exception.getMessage());

        verify(
                pagoRepository,
                never())
                .findByReserva_IdReservaOrderByFechaCreacionDesc(
                        99);
    }

    @Test
    void listarPagosIdReservaInvalidoEsRechazado() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                pagoService.listarPagosPropios(
                                        "cliente@bembos.test",
                                        0));

        assertEquals(
                "El id de reserva debe ser positivo",
                exception.getMessage());
    }

    private Pago crearPago(
            Integer idPago,
            Reserva reserva,
            String monto,
            LocalDateTime fechaCreacion) {

        Pago pago =
                new Pago();

        pago.setIdPago(
                idPago);

        pago.setReserva(
                reserva);

        pago.setMonto(
                new BigDecimal(monto));

        pago.setMetodoPago(
                "PRUEBA");

        pago.setEstadoPago(
                "PENDIENTE");

        pago.setFechaCreacion(
                fechaCreacion);

        return pago;
    }
}