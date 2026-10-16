package com.bembosevents.service.impl;

import com.bembosevents.dto.catalogo.DisponibilidadSalonResponse;
import com.bembosevents.entity.HorarioSalon;
import com.bembosevents.entity.Salon;
import com.bembosevents.exception.RecursoNoEncontradoException;
import com.bembosevents.repository.HorarioSalonRepository;
import com.bembosevents.repository.SalonRepository;
import com.bembosevents.service.DisponibilidadSalonService;
import com.bembosevents.service.ReservaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class DisponibilidadSalonServiceImpl
        implements DisponibilidadSalonService {

    private static final String MENSAJE_SOLAPAMIENTO =
            "El salon no esta disponible en el horario solicitado";

    private final SalonRepository salonRepository;
    private final HorarioSalonRepository horarioSalonRepository;
    private final ReservaService reservaService;

    public DisponibilidadSalonServiceImpl(
            SalonRepository salonRepository,
            HorarioSalonRepository horarioSalonRepository,
            ReservaService reservaService) {

        this.salonRepository = salonRepository;
        this.horarioSalonRepository = horarioSalonRepository;
        this.reservaService = reservaService;
    }

    @Override
    public DisponibilidadSalonResponse consultar(
            Integer idSalon,
            LocalDate fecha,
            LocalTime horaInicio,
            LocalTime horaFin) {

        validarParametros(
                idSalon,
                fecha,
                horaInicio,
                horaFin);

        Salon salon = buscarSalonPublico(idSalon);

        List<HorarioSalon> horarios =
                horarioSalonRepository
                        .findBySalon_IdSalonAndActivoTrue(
                                idSalon);

        boolean dentroHorario = horarios.stream()
                .anyMatch(horario ->
                        coincideDia(
                                horario.getDiaSemana(),
                                fecha)
                                && cubreHorario(
                                        horario,
                                        horaInicio,
                                        horaFin));

        if (!dentroHorario) {

            return respuesta(
                    salon,
                    fecha,
                    horaInicio,
                    horaFin,
                    false,
                    "Horario fuera de la disponibilidad configurada");
        }

        try {

            reservaService.validarDisponibilidad(
                    idSalon,
                    fecha,
                    horaInicio,
                    horaFin);

            return respuesta(
                    salon,
                    fecha,
                    horaInicio,
                    horaFin,
                    true,
                    "Disponible");

        }
        catch (IllegalArgumentException ex) {

            if (MENSAJE_SOLAPAMIENTO.equals(
                    ex.getMessage())) {

                return respuesta(
                        salon,
                        fecha,
                        horaInicio,
                        horaFin,
                        false,
                        MENSAJE_SOLAPAMIENTO);
            }

            throw ex;
        }
    }

    private void validarParametros(
            Integer idSalon,
            LocalDate fecha,
            LocalTime horaInicio,
            LocalTime horaFin) {

        if (idSalon == null
                || fecha == null
                || horaInicio == null
                || horaFin == null) {

            throw new IllegalArgumentException(
                    "Salon, fecha y horario son obligatorios");
        }

        if (!horaInicio.isBefore(horaFin)) {

            throw new IllegalArgumentException(
                    "La hora de inicio debe ser anterior a la hora de fin");
        }
    }

    private Salon buscarSalonPublico(
            Integer idSalon) {

        Salon salon = salonRepository
                .findByIdSalonAndActivoTrue(idSalon)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Salon no encontrado"));

        if (salon.getSede() == null
                || !Boolean.TRUE.equals(
                        salon.getSede().getActivo())) {

            throw new RecursoNoEncontradoException(
                    "Salon no encontrado");
        }

        return salon;
    }

    private boolean coincideDia(
            String diaHorario,
            LocalDate fecha) {

        return normalizarDia(diaHorario)
                .equals(diaFecha(fecha));
    }

    private boolean cubreHorario(
            HorarioSalon horario,
            LocalTime horaInicio,
            LocalTime horaFin) {

        if (horario.getHoraInicio() == null
                || horario.getHoraFin() == null) {

            return false;
        }

        return !horaInicio.isBefore(
                horario.getHoraInicio())
                && !horaFin.isAfter(
                        horario.getHoraFin());
    }

    private String diaFecha(
            LocalDate fecha) {

        return switch (fecha.getDayOfWeek()) {
            case MONDAY -> "LUNES";
            case TUESDAY -> "MARTES";
            case WEDNESDAY -> "MIERCOLES";
            case THURSDAY -> "JUEVES";
            case FRIDAY -> "VIERNES";
            case SATURDAY -> "SABADO";
            case SUNDAY -> "DOMINGO";
        };
    }

    private String normalizarDia(
            String value) {

        if (value == null) {
            return "";
        }

        String normalized = Normalizer.normalize(
                value.strip(),
                Normalizer.Form.NFD);

        return normalized
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT);
    }

    private DisponibilidadSalonResponse respuesta(
            Salon salon,
            LocalDate fecha,
            LocalTime horaInicio,
            LocalTime horaFin,
            boolean disponible,
            String motivo) {

        return new DisponibilidadSalonResponse(
                salon.getIdSalon(),
                fecha,
                horaInicio,
                horaFin,
                disponible,
                motivo);
    }
}
