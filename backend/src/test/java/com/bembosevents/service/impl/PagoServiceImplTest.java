package com.bembosevents.service.impl;

import com.bembosevents.domain.EstadoPago;
import com.bembosevents.dto.pago.CrearPagoRequest;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PagoServiceImplTest {

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
    void iniciarPagoPropietarioCreaPagoPendiente() {

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
                pagoRepository.save(
                        any(Pago.class))
        ).thenAnswer(invocation -> {

            Pago pago =
                    invocation.getArgument(0);

            pago.setIdPago(30);

            return pago;
        });

        CrearPagoRequest request =
                new CrearPagoRequest(
                        new BigDecimal("250.00"),
                        "  PRUEBA  ");

        PagoResponse response =
                pagoService.iniciarPago(
                        "cliente@bembos.test",
                        20,
                        request);

        assertEquals(
                30,
                response.idPago());

        assertEquals(
                20,
                response.idReserva());

        assertEquals(
                new BigDecimal("250.00"),
                response.monto());

        assertEquals(
                "PRUEBA",
                response.metodoPago());

        assertEquals(
                EstadoPago.PENDIENTE.name(),
                response.estadoPago());

        assertNull(
                response.referenciaExterna());

        assertNull(
                response.fechaPago());

        assertNotNull(
                response.fechaCreacion());

        ArgumentCaptor<Pago> captor =
                ArgumentCaptor.forClass(
                        Pago.class);

        verify(
                pagoRepository)
                .save(
                        captor.capture());

        Pago guardado =
                captor.getValue();

        assertEquals(
                reserva,
                guardado.getReserva());

        assertEquals(
                new BigDecimal("250.00"),
                guardado.getMonto());

        assertEquals(
                "PRUEBA",
                guardado.getMetodoPago());

        assertEquals(
                EstadoPago.PENDIENTE.name(),
                guardado.getEstadoPago());

        assertNull(
                guardado.getReferenciaExterna());

        assertNull(
                guardado.getFechaPago());

        assertNotNull(
                guardado.getFechaCreacion());
    }

    @Test
    void iniciarPagoReservaAjenaRespondeNoEncontrada() {

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

        CrearPagoRequest request =
                new CrearPagoRequest(
                        new BigDecimal("100.00"),
                        "PRUEBA");

        RecursoNoEncontradoException exception =
                assertThrows(
                        RecursoNoEncontradoException.class,
                        () ->
                                pagoService.iniciarPago(
                                        "cliente@bembos.test",
                                        99,
                                        request));

        assertEquals(
                "Reserva no encontrada",
                exception.getMessage());

        verify(
                pagoRepository,
                never())
                .save(
                        any(Pago.class));
    }

    @Test
    void iniciarPagoMontoCeroEsRechazado() {

        CrearPagoRequest request =
                new CrearPagoRequest(
                        BigDecimal.ZERO,
                        "PRUEBA");

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                pagoService.iniciarPago(
                                        "cliente@bembos.test",
                                        20,
                                        request));

        assertEquals(
                "El monto debe ser mayor que 0",
                exception.getMessage());

        verifyNoInteractions(
                clienteRepository,
                reservaRepository,
                pagoRepository);
    }

    @Test
    void iniciarPagoMontoNegativoEsRechazado() {

        CrearPagoRequest request =
                new CrearPagoRequest(
                        new BigDecimal("-1.00"),
                        "PRUEBA");

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        pagoService.iniciarPago(
                                "cliente@bembos.test",
                                20,
                                request));

        verifyNoInteractions(
                clienteRepository,
                reservaRepository,
                pagoRepository);
    }

    @Test
    void iniciarPagoMetodoVacioEsRechazado() {

        CrearPagoRequest request =
                new CrearPagoRequest(
                        new BigDecimal("100.00"),
                        "   ");

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                pagoService.iniciarPago(
                                        "cliente@bembos.test",
                                        20,
                                        request));

        assertEquals(
                "El metodo de pago es obligatorio",
                exception.getMessage());

        verifyNoInteractions(
                clienteRepository,
                reservaRepository,
                pagoRepository);
    }

    @Test
    void iniciarPagoIdReservaInvalidoEsRechazado() {

        CrearPagoRequest request =
                new CrearPagoRequest(
                        new BigDecimal("100.00"),
                        "PRUEBA");

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                pagoService.iniciarPago(
                                        "cliente@bembos.test",
                                        0,
                                        request));

        assertEquals(
                "El id de reserva debe ser positivo",
                exception.getMessage());

        verifyNoInteractions(
                clienteRepository,
                reservaRepository,
                pagoRepository);
    }
}