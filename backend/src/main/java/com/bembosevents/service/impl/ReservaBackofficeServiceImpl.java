package com.bembosevents.service.impl;

import com.bembosevents.domain.EstadoReserva;
import com.bembosevents.dto.reserva.CambiarEstadoReservaRequest;
import com.bembosevents.dto.reserva.ReservaDetalleResponse;
import com.bembosevents.dto.reserva.ReservaDetalleServicioResponse;
import com.bembosevents.dto.reserva.ReservaResumenResponse;
import com.bembosevents.entity.HistorialReserva;
import com.bembosevents.entity.Reserva;
import com.bembosevents.entity.ReservaServicio;
import com.bembosevents.entity.Usuario;
import com.bembosevents.exception.ConflictoReservaException;
import com.bembosevents.exception.RecursoNoEncontradoException;
import com.bembosevents.repository.HistorialReservaRepository;
import com.bembosevents.repository.ReservaRepository;
import com.bembosevents.repository.UsuarioRepository;
import com.bembosevents.service.ReservaBackofficeService;
import com.bembosevents.service.ReservaService;
import com.bembosevents.service.SedeAccessService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReservaBackofficeServiceImpl
        implements ReservaBackofficeService {

    private final ReservaRepository reservaRepository;
    private final SedeAccessService sedeAccessService;
    private final ReservaService reservaService;
    private final HistorialReservaRepository historialReservaRepository;
    private final UsuarioRepository usuarioRepository;

    public ReservaBackofficeServiceImpl(
            ReservaRepository reservaRepository,
            SedeAccessService sedeAccessService,
            ReservaService reservaService,
            HistorialReservaRepository historialReservaRepository,
            UsuarioRepository usuarioRepository) {

        this.reservaRepository = reservaRepository;
        this.sedeAccessService = sedeAccessService;
        this.reservaService = reservaService;
        this.historialReservaRepository = historialReservaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public List<ReservaResumenResponse> listarAccesibles(
            Authentication authentication) {

        if (sedeAccessService
                .esAdministrador(authentication)) {

            return reservaRepository
                    .findAllByOrderByFechaCreacionDesc()
                    .stream()
                    .map(this::convertirResumen)
                    .toList();
        }

        if (sedeAccessService
                .esEncargadoLocal(authentication)) {

            Integer idSede =
                    sedeAccessService
                            .obtenerSedeAsignada(
                                    authentication.getName());

            return reservaRepository
                    .findBySede_IdSedeOrderByFechaCreacionDesc(
                            idSede)
                    .stream()
                    .map(this::convertirResumen)
                    .toList();
        }

        throw new AccessDeniedException(
                "No tiene permisos para acceder al BackOffice");
    }

    @Override
    public ReservaDetalleResponse obtenerAccesible(
            Integer idReserva,
            Authentication authentication) {

        validarIdReserva(idReserva);

        Reserva reserva;

        if (sedeAccessService
                .esAdministrador(authentication)) {

            reserva = reservaRepository
                    .findById(idReserva)
                    .orElseThrow(() ->
                            new RecursoNoEncontradoException(
                                    "Reserva no encontrada"));
        }
        else if (sedeAccessService
                .esEncargadoLocal(authentication)) {

            Integer idSede =
                    sedeAccessService
                            .obtenerSedeAsignada(
                                    authentication.getName());

            reserva = reservaRepository
                    .findByIdReservaAndSede_IdSede(
                            idReserva,
                            idSede)
                    .orElseThrow(() ->
                            new RecursoNoEncontradoException(
                                    "Reserva no encontrada"));
        }
        else {
            throw new AccessDeniedException(
                    "No tiene permisos para acceder al BackOffice");
        }

        return convertirDetalle(reserva);
    }

    @Override
    @Transactional
    public ReservaDetalleResponse cambiarEstado(
            Integer idReserva,
            CambiarEstadoReservaRequest request,
            Authentication authentication) {

        validarIdReserva(idReserva);

        if (request == null ||
                request.nuevoEstado() == null) {

            throw new IllegalArgumentException(
                    "El nuevo estado es obligatorio");
        }

        Reserva reserva =
                buscarParaActualizacion(
                        idReserva,
                        authentication);

        EstadoReserva estadoActual;

        try {
            estadoActual =
                    EstadoReserva.valueOf(
                            reserva.getEstado());
        }
        catch (IllegalArgumentException |
                NullPointerException ex) {

            throw new IllegalStateException(
                    "La reserva tiene un estado invalido");
        }

        try {
            reservaService.validarTransicionEstado(
                    estadoActual,
                    request.nuevoEstado());
        }
        catch (IllegalArgumentException ex) {

            throw new ConflictoReservaException(
                    "La transicion de estado de la reserva no esta permitida");
        }

        Usuario usuarioResponsable =
                usuarioRepository
                        .findByCorreoIgnoreCase(
                                authentication.getName())
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "El usuario autenticado no existe"));

        LocalDateTime ahora =
                LocalDateTime.now();

        reserva.setEstado(
                request.nuevoEstado().name());

        reserva.setFechaActualizacion(
                ahora);

        Reserva reservaGuardada =
                reservaRepository.save(
                        reserva);

        HistorialReserva historial =
                new HistorialReserva();

        historial.setReserva(
                reservaGuardada);

        historial.setEstadoAnterior(
                estadoActual.name());

        historial.setEstadoNuevo(
                request.nuevoEstado().name());

        historial.setObservacion(
                "Estado de reserva actualizado desde BackOffice");

        historial.setFechaCambio(
                ahora);

        historial.setUsuarioResponsable(
                usuarioResponsable);

        historialReservaRepository.save(
                historial);

        return convertirDetalle(
                reservaGuardada);
    }

    private Reserva buscarParaActualizacion(
            Integer idReserva,
            Authentication authentication) {

        if (sedeAccessService
                .esAdministrador(authentication)) {

            return reservaRepository
                    .findByIdReservaForUpdate(
                            idReserva)
                    .orElseThrow(() ->
                            new RecursoNoEncontradoException(
                                    "Reserva no encontrada"));
        }

        if (sedeAccessService
                .esEncargadoLocal(authentication)) {

            Integer idSede =
                    sedeAccessService
                            .obtenerSedeAsignada(
                                    authentication.getName());

            return reservaRepository
                    .findByIdReservaAndSede_IdSedeForUpdate(
                            idReserva,
                            idSede)
                    .orElseThrow(() ->
                            new RecursoNoEncontradoException(
                                    "Reserva no encontrada"));
        }

        throw new AccessDeniedException(
                "No tiene permisos para acceder al BackOffice");
    }

    private void validarIdReserva(
            Integer idReserva) {

        if (idReserva == null ||
                idReserva <= 0) {

            throw new IllegalArgumentException(
                    "El id de reserva debe ser positivo");
        }
    }

    private ReservaResumenResponse convertirResumen(
            Reserva reserva) {

        return new ReservaResumenResponse(
                reserva.getIdReserva(),
                reserva.getCodigoReserva(),
                reserva.getEstado(),
                reserva.getSede().getIdSede(),
                reserva.getSalon().getIdSalon(),
                reserva.getPaquete() != null
                        ? reserva.getPaquete().getIdPaquete()
                        : null,
                reserva.getTematica() != null
                        ? reserva.getTematica().getIdTematica()
                        : null,
                reserva.getFechaEvento(),
                reserva.getHoraInicio(),
                reserva.getHoraFin(),
                reserva.getCantidadInvitados(),
                reserva.getMontoTotal(),
                reserva.getMontoDeposito(),
                reserva.getFechaCreacion());
    }

    private ReservaDetalleResponse convertirDetalle(
            Reserva reserva) {

        List<ReservaDetalleServicioResponse> servicios =
                reserva.getReservaServicios() == null
                        ? List.of()
                        : reserva.getReservaServicios()
                                .stream()
                                .map(this::convertirServicio)
                                .toList();

        return new ReservaDetalleResponse(
                reserva.getIdReserva(),
                reserva.getCodigoReserva(),
                reserva.getEstado(),
                reserva.getSede().getIdSede(),
                reserva.getSalon().getIdSalon(),
                reserva.getPaquete() != null
                        ? reserva.getPaquete().getIdPaquete()
                        : null,
                reserva.getTematica() != null
                        ? reserva.getTematica().getIdTematica()
                        : null,
                reserva.getFechaEvento(),
                reserva.getHoraInicio(),
                reserva.getHoraFin(),
                reserva.getCantidadInvitados(),
                reserva.getMontoBase(),
                reserva.getMontoServicios(),
                reserva.getMontoTotal(),
                reserva.getPorcentajeDeposito(),
                reserva.getMontoDeposito(),
                servicios,
                reserva.getFechaCreacion(),
                reserva.getFechaActualizacion());
    }

    private ReservaDetalleServicioResponse convertirServicio(
            ReservaServicio detalle) {

        return new ReservaDetalleServicioResponse(
                detalle.getServicioAdicional()
                        .getIdServicioAdicional(),
                detalle.getCantidad(),
                detalle.getPrecioUnitario(),
                detalle.getSubtotal());
    }
}