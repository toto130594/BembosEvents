package com.bembosevents.service;

import com.bembosevents.config.ReservaProperties;
import com.bembosevents.domain.EstadoReserva;
import com.bembosevents.entity.Reserva;
import com.bembosevents.entity.Salon;
import com.bembosevents.repository.ReservaRepository;
import com.bembosevents.repository.SalonRepository;
import com.bembosevents.service.impl.ReservaServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservaServiceImplTest {

    @Mock
    private SalonRepository salonRepository;

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private ReservaProperties reservaProperties;

    @InjectMocks
    private ReservaServiceImpl reservaService;

    @Test
    void debeAceptarCantidadDentroDeCapacidad() {

        Salon salon = new Salon();
        salon.setCapacidad(50);

        when(salonRepository.findById(1))
                .thenReturn(Optional.of(salon));

        assertDoesNotThrow(
                () -> reservaService.validarCapacidad(1, 40));

        verify(salonRepository).findById(1);
    }

    @Test
    void debeAceptarCantidadIgualALaCapacidad() {

        Salon salon = new Salon();
        salon.setCapacidad(50);

        when(salonRepository.findById(1))
                .thenReturn(Optional.of(salon));

        assertDoesNotThrow(
                () -> reservaService.validarCapacidad(1, 50));
    }

    @Test
    void debeRechazarCantidadSuperiorALaCapacidad() {

        Salon salon = new Salon();
        salon.setCapacidad(50);

        when(salonRepository.findById(1))
                .thenReturn(Optional.of(salon));

        assertThrows(
                IllegalArgumentException.class,
                () -> reservaService.validarCapacidad(1, 51));
    }

    @Test
    void debeRechazarSalonInexistente() {

        when(salonRepository.findById(99))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> reservaService.validarCapacidad(99, 30));
    }

    @Test
    void debeRechazarCantidadCeroSinConsultarRepository() {

        assertThrows(
                IllegalArgumentException.class,
                () -> reservaService.validarCapacidad(1, 0));

        verify(salonRepository, never()).findById(1);
    }

    @Test
    void debeAceptarHorarioDisponible() {

        LocalDate fecha = LocalDate.of(2026, 10, 20);

        Reserva existente = new Reserva();
        existente.setHoraInicio(LocalTime.of(10, 0));
        existente.setHoraFin(LocalTime.of(12, 0));

        when(reservaRepository
                .findBySalon_IdSalonAndFechaEventoAndEstadoIn(
                        org.mockito.ArgumentMatchers.eq(1),
                        org.mockito.ArgumentMatchers.eq(fecha),
                        anyCollection()))
                .thenReturn(List.of(existente));

        assertDoesNotThrow(
                () -> reservaService.validarDisponibilidad(
                        1,
                        fecha,
                        LocalTime.of(12, 0),
                        LocalTime.of(14, 0)));
    }

    @Test
    void debeRechazarHorarioConSolapamiento() {

        LocalDate fecha = LocalDate.of(2026, 10, 20);

        Reserva existente = new Reserva();
        existente.setHoraInicio(LocalTime.of(10, 0));
        existente.setHoraFin(LocalTime.of(12, 0));

        when(reservaRepository
                .findBySalon_IdSalonAndFechaEventoAndEstadoIn(
                        org.mockito.ArgumentMatchers.eq(1),
                        org.mockito.ArgumentMatchers.eq(fecha),
                        anyCollection()))
                .thenReturn(List.of(existente));

        assertThrows(
                IllegalArgumentException.class,
                () -> reservaService.validarDisponibilidad(
                        1,
                        fecha,
                        LocalTime.of(11, 0),
                        LocalTime.of(13, 0)));
    }

    @Test
    void debeRechazarHorarioInvalidoSinConsultarRepository() {

        assertThrows(
                IllegalArgumentException.class,
                () -> reservaService.validarDisponibilidad(
                        1,
                        LocalDate.of(2026, 10, 20),
                        LocalTime.of(18, 0),
                        LocalTime.of(16, 0)));

        verifyNoInteractions(reservaRepository);
    }

    @Test
    void debeAceptarMontoMinimoValido() {

        when(reservaProperties.getMontoMinimo())
                .thenReturn(new BigDecimal("500.00"));

        assertDoesNotThrow(
                () -> reservaService.validarMontoMinimo(
                        new BigDecimal("500.00")));
    }

    @Test
    void debeRechazarMontoMenorAlMinimo() {

        when(reservaProperties.getMontoMinimo())
                .thenReturn(new BigDecimal("500.00"));

        assertThrows(
                IllegalArgumentException.class,
                () -> reservaService.validarMontoMinimo(
                        new BigDecimal("499.99")));
    }

    @Test
    void debeCalcularMontoTotal() {

        BigDecimal resultado = reservaService.calcularMontoTotal(
                new BigDecimal("500.00"),
                new BigDecimal("150.00"));

        assertEquals(
                new BigDecimal("650.00"),
                resultado);
    }

    @Test
    void debeCalcularMontoDepositoConfigurado() {

        when(reservaProperties.getPorcentajeDeposito())
                .thenReturn(new BigDecimal("0.50"));

        BigDecimal resultado =
                reservaService.calcularMontoDeposito(
                        new BigDecimal("1000.00"));

        assertEquals(
                new BigDecimal("500.00"),
                resultado);
    }

    @Test
    void debeAceptarTransicionDeReservaValida() {

        assertDoesNotThrow(
                () -> reservaService.validarTransicionEstado(
                        EstadoReserva.PENDIENTE,
                        EstadoReserva.CONFIRMADA));
    }

    @Test
    void debeRechazarTransicionDeReservaInvalida() {

        assertThrows(
                IllegalArgumentException.class,
                () -> reservaService.validarTransicionEstado(
                        EstadoReserva.COMPLETADA,
                        EstadoReserva.PENDIENTE));
    }
}