package com.bembosevents.service.impl;

import com.bembosevents.config.ReservaProperties;
import com.bembosevents.domain.EstadoReserva;
import com.bembosevents.domain.ReglasReserva;
import com.bembosevents.domain.ReglasTransicionReserva;
import com.bembosevents.entity.Reserva;
import com.bembosevents.entity.Salon;
import com.bembosevents.repository.ReservaRepository;
import com.bembosevents.repository.SalonRepository;
import com.bembosevents.service.ReservaService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@Service
public class ReservaServiceImpl implements ReservaService {

    private static final Set<String> ESTADOS_BLOQUEANTES = Set.of(
            EstadoReserva.PENDIENTE.name(),
            EstadoReserva.CONFIRMADA.name(),
            EstadoReserva.EN_PROCESO.name()
    );

    private final SalonRepository salonRepository;
    private final ReservaRepository reservaRepository;
    private final ReservaProperties reservaProperties;

    public ReservaServiceImpl(
            SalonRepository salonRepository,
            ReservaRepository reservaRepository,
            ReservaProperties reservaProperties) {

        this.salonRepository = salonRepository;
        this.reservaRepository = reservaRepository;
        this.reservaProperties = reservaProperties;
    }

    @Override
    public void validarCapacidad(
            Integer idSalon,
            Integer cantidadInvitados) {

        if (idSalon == null || cantidadInvitados == null) {
            throw new IllegalArgumentException(
                    "El salon y la cantidad de invitados son obligatorios");
        }

        if (cantidadInvitados <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad de invitados debe ser mayor que cero");
        }

        Salon salon = salonRepository.findById(idSalon)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "El salon no existe"));

        if (salon.getCapacidad() == null || salon.getCapacidad() <= 0) {
            throw new IllegalArgumentException(
                    "El salon no tiene una capacidad valida");
        }

        if (!ReglasReserva.cumpleCapacidad(
                cantidadInvitados,
                salon.getCapacidad())) {

            throw new IllegalArgumentException(
                    "La cantidad de invitados supera la capacidad del salon");
        }
    }

    @Override
    public void validarDisponibilidad(
            Integer idSalon,
            LocalDate fechaEvento,
            LocalTime horaInicio,
            LocalTime horaFin) {

        if (idSalon == null
                || fechaEvento == null
                || horaInicio == null
                || horaFin == null) {

            throw new IllegalArgumentException(
                    "Salon, fecha y horario son obligatorios");
        }

        if (!horaInicio.isBefore(horaFin)) {
            throw new IllegalArgumentException(
                    "La hora de inicio debe ser anterior a la hora de fin");
        }

        List<Reserva> reservas = reservaRepository
                .findBySalon_IdSalonAndFechaEventoAndEstadoIn(
                        idSalon,
                        fechaEvento,
                        ESTADOS_BLOQUEANTES);

        LocalDateTime inicioNuevo =
                LocalDateTime.of(fechaEvento, horaInicio);

        LocalDateTime finNuevo =
                LocalDateTime.of(fechaEvento, horaFin);

        boolean existeSolapamiento = reservas.stream()
                .anyMatch(reserva -> {

                    if (reserva.getHoraInicio() == null
                            || reserva.getHoraFin() == null) {
                        return false;
                    }

                    LocalDateTime inicioExistente =
                            LocalDateTime.of(
                                    fechaEvento,
                                    reserva.getHoraInicio());

                    LocalDateTime finExistente =
                            LocalDateTime.of(
                                    fechaEvento,
                                    reserva.getHoraFin());

                    return ReglasReserva.haySolapamiento(
                            inicioExistente,
                            finExistente,
                            inicioNuevo,
                            finNuevo);
                });

        if (existeSolapamiento) {
            throw new IllegalArgumentException(
                    "El salon no esta disponible en el horario solicitado");
        }
    }

    @Override
    public void validarMontoMinimo(
            BigDecimal montoTotal) {

        if (!ReglasReserva.cumpleMontoMinimo(
                montoTotal,
                reservaProperties.getMontoMinimo())) {

            throw new IllegalArgumentException(
                    "El monto total no cumple el minimo requerido");
        }
    }

    @Override
    public BigDecimal calcularMontoTotal(
            BigDecimal montoBase,
            BigDecimal montoServicios) {

        return ReglasReserva.calcularMontoTotal(
                montoBase,
                montoServicios);
    }

    @Override
    public BigDecimal calcularMontoDeposito(
            BigDecimal montoTotal) {

        return ReglasReserva.calcularAdelanto(
                montoTotal,
                reservaProperties.getPorcentajeDeposito());
    }

    @Override
    public void validarTransicionEstado(
            EstadoReserva estadoActual,
            EstadoReserva nuevoEstado) {

        if (!ReglasTransicionReserva.esValida(
                estadoActual,
                nuevoEstado)) {

            throw new IllegalArgumentException(
                    "Transicion de estado de reserva no permitida");
        }
    }
}