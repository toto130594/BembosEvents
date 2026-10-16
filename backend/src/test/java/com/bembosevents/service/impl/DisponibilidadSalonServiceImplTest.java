package com.bembosevents.service.impl;

import com.bembosevents.entity.HorarioSalon;
import com.bembosevents.entity.Salon;
import com.bembosevents.entity.Sede;
import com.bembosevents.exception.RecursoNoEncontradoException;
import com.bembosevents.repository.HorarioSalonRepository;
import com.bembosevents.repository.SalonRepository;
import com.bembosevents.service.ReservaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DisponibilidadSalonServiceImplTest {

    @Mock
    private SalonRepository salonRepository;

    @Mock
    private HorarioSalonRepository horarioSalonRepository;

    @Mock
    private ReservaService reservaService;

    private DisponibilidadSalonServiceImpl service;

    @BeforeEach
    void setUp() {

        service = new DisponibilidadSalonServiceImpl(
                salonRepository,
                horarioSalonRepository,
                reservaService);
    }

    @Test
    void debeEstarDisponibleSinSolapamiento() {

        LocalDate fecha = LocalDate.of(2026, 10, 19);

        prepararSalonYHorario(
                fecha,
                LocalTime.of(9, 0),
                LocalTime.of(22, 0));

        var resultado = service.consultar(
                1,
                fecha,
                LocalTime.of(14, 0),
                LocalTime.of(18, 0));

        assertTrue(resultado.disponible());

        verify(reservaService).validarDisponibilidad(
                1,
                fecha,
                LocalTime.of(14, 0),
                LocalTime.of(18, 0));
    }

    @Test
    void debeRechazarHorarioFueraDeConfiguracion() {

        LocalDate fecha = LocalDate.of(2026, 10, 19);

        prepararSalonYHorario(
                fecha,
                LocalTime.of(9, 0),
                LocalTime.of(17, 0));

        var resultado = service.consultar(
                1,
                fecha,
                LocalTime.of(16, 0),
                LocalTime.of(18, 0));

        assertFalse(resultado.disponible());

        verify(reservaService, never())
                .validarDisponibilidad(
                        1,
                        fecha,
                        LocalTime.of(16, 0),
                        LocalTime.of(18, 0));
    }

    @Test
    void debeResponderNoDisponibleSiExisteSolapamiento() {

        LocalDate fecha = LocalDate.of(2026, 10, 19);

        prepararSalonYHorario(
                fecha,
                LocalTime.of(9, 0),
                LocalTime.of(22, 0));

        doThrow(new IllegalArgumentException(
                "El salon no esta disponible en el horario solicitado"))
                .when(reservaService)
                .validarDisponibilidad(
                        1,
                        fecha,
                        LocalTime.of(14, 0),
                        LocalTime.of(18, 0));

        var resultado = service.consultar(
                1,
                fecha,
                LocalTime.of(14, 0),
                LocalTime.of(18, 0));

        assertFalse(resultado.disponible());
    }

    @Test
    void debeOcultarSalonInexistente() {

        when(salonRepository
                .findByIdSalonAndActivoTrue(99))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> service.consultar(
                        99,
                        LocalDate.of(2026, 10, 19),
                        LocalTime.of(14, 0),
                        LocalTime.of(18, 0)));
    }

    private void prepararSalonYHorario(
            LocalDate fecha,
            LocalTime apertura,
            LocalTime cierre) {

        Sede sede = new Sede();
        sede.setIdSede(1);
        sede.setActivo(true);

        Salon salon = new Salon();
        salon.setIdSalon(1);
        salon.setSede(sede);
        salon.setActivo(true);

        HorarioSalon horario = new HorarioSalon();
        horario.setIdHorarioSalon(1);
        horario.setSalon(salon);
        horario.setDiaSemana(dia(fecha));
        horario.setHoraInicio(apertura);
        horario.setHoraFin(cierre);
        horario.setActivo(true);

        when(salonRepository
                .findByIdSalonAndActivoTrue(1))
                .thenReturn(Optional.of(salon));

        when(horarioSalonRepository
                .findBySalon_IdSalonAndActivoTrue(1))
                .thenReturn(List.of(horario));
    }

    private String dia(
            LocalDate fecha) {

        return switch (fecha.getDayOfWeek()) {
            case MONDAY -> "Lunes";
            case TUESDAY -> "Martes";
            case WEDNESDAY -> "Miercoles";
            case THURSDAY -> "Jueves";
            case FRIDAY -> "Viernes";
            case SATURDAY -> "Sabado";
            case SUNDAY -> "Domingo";
        };
    }
}
