package com.bembosevents.service.impl;

import com.bembosevents.config.ReservaProperties;
import com.bembosevents.domain.EstadoReserva;
import com.bembosevents.dto.catalogo.DisponibilidadSalonResponse;
import com.bembosevents.dto.reserva.CrearReservaRequest;
import com.bembosevents.dto.reserva.CrearReservaServicioRequest;
import com.bembosevents.dto.reserva.ReservaCreadaResponse;
import com.bembosevents.entity.Cliente;
import com.bembosevents.entity.HistorialReserva;
import com.bembosevents.entity.Paquete;
import com.bembosevents.entity.Reserva;
import com.bembosevents.entity.ReservaServicio;
import com.bembosevents.entity.Salon;
import com.bembosevents.entity.Sede;
import com.bembosevents.entity.ServicioAdicional;
import com.bembosevents.entity.Tematica;
import com.bembosevents.exception.ConflictoReservaException;
import com.bembosevents.exception.RecursoNoEncontradoException;
import com.bembosevents.repository.ClienteRepository;
import com.bembosevents.repository.HistorialReservaRepository;
import com.bembosevents.repository.PaqueteRepository;
import com.bembosevents.repository.ReservaRepository;
import com.bembosevents.repository.ReservaServicioRepository;
import com.bembosevents.repository.SalonRepository;
import com.bembosevents.repository.SedeRepository;
import com.bembosevents.repository.ServicioAdicionalRepository;
import com.bembosevents.repository.TematicaRepository;
import com.bembosevents.service.DisponibilidadSalonService;
import com.bembosevents.service.ReservaClienteService;
import com.bembosevents.service.ReservaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

@Service
@Transactional
public class ReservaClienteServiceImpl
        implements ReservaClienteService {

    private final ClienteRepository clienteRepository;
    private final SedeRepository sedeRepository;
    private final SalonRepository salonRepository;
    private final PaqueteRepository paqueteRepository;
    private final TematicaRepository tematicaRepository;
    private final ServicioAdicionalRepository servicioAdicionalRepository;
    private final ReservaRepository reservaRepository;
    private final ReservaServicioRepository reservaServicioRepository;
    private final HistorialReservaRepository historialReservaRepository;
    private final DisponibilidadSalonService disponibilidadSalonService;
    private final ReservaService reservaService;
    private final ReservaProperties reservaProperties;

    public ReservaClienteServiceImpl(
            ClienteRepository clienteRepository,
            SedeRepository sedeRepository,
            SalonRepository salonRepository,
            PaqueteRepository paqueteRepository,
            TematicaRepository tematicaRepository,
            ServicioAdicionalRepository servicioAdicionalRepository,
            ReservaRepository reservaRepository,
            ReservaServicioRepository reservaServicioRepository,
            HistorialReservaRepository historialReservaRepository,
            DisponibilidadSalonService disponibilidadSalonService,
            ReservaService reservaService,
            ReservaProperties reservaProperties) {

        this.clienteRepository = clienteRepository;
        this.sedeRepository = sedeRepository;
        this.salonRepository = salonRepository;
        this.paqueteRepository = paqueteRepository;
        this.tematicaRepository = tematicaRepository;
        this.servicioAdicionalRepository = servicioAdicionalRepository;
        this.reservaRepository = reservaRepository;
        this.reservaServicioRepository = reservaServicioRepository;
        this.historialReservaRepository = historialReservaRepository;
        this.disponibilidadSalonService = disponibilidadSalonService;
        this.reservaService = reservaService;
        this.reservaProperties = reservaProperties;
    }

    @Override
    public ReservaCreadaResponse crear(
            String correoCliente,
            CrearReservaRequest request) {

        if (correoCliente == null
                || correoCliente.isBlank()) {

            throw new IllegalArgumentException(
                    "El cliente autenticado es obligatorio");
        }

        Cliente cliente = clienteRepository
                .findByUsuario_CorreoIgnoreCaseAndActivoTrue(
                        correoCliente.strip())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "El usuario autenticado no tiene un perfil Cliente activo"));

        if (cliente.getUsuario() == null) {
            throw new IllegalStateException(
                    "El perfil Cliente no tiene un Usuario asociado");
        }

        Sede sede = sedeRepository
                .findByIdSedeAndActivoTrue(
                        request.idSede())
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Sede no encontrada"));

        Salon salon = salonRepository
                .findByIdSalonAndActivoTrueForUpdate(
                        request.idSalon())
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Salon no encontrado"));

        validarSalonPerteneceSede(
                salon,
                sede);

        reservaService.validarCapacidad(
                salon.getIdSalon(),
                request.cantidadInvitados());

        DisponibilidadSalonResponse disponibilidad =
                disponibilidadSalonService.consultar(
                        salon.getIdSalon(),
                        request.fechaEvento(),
                        request.horaInicio(),
                        request.horaFin());

        if (!disponibilidad.disponible()) {
            throw new ConflictoReservaException(
                    disponibilidad.motivo());
        }

        Paquete paquete =
                buscarPaquete(request.idPaquete());

        Tematica tematica =
                buscarTematica(request.idTematica());

        List<ServicioSeleccionado> servicios =
                buscarServicios(
                        request.serviciosAdicionales());

        BigDecimal montoBase =
                calcularMontoBase(
                        paquete,
                        tematica);

        BigDecimal montoServicios = servicios
                .stream()
                .map(ServicioSeleccionado::subtotal)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add);

        BigDecimal montoTotal =
                reservaService.calcularMontoTotal(
                        montoBase,
                        montoServicios);

        reservaService.validarMontoMinimo(
                montoTotal);

        BigDecimal porcentajeDeposito =
                reservaProperties
                        .getPorcentajeDeposito();

        if (porcentajeDeposito == null) {
            throw new IllegalStateException(
                    "El porcentaje de deposito no esta configurado");
        }

        BigDecimal montoDeposito =
                reservaService.calcularMontoDeposito(
                        montoTotal);

        Long consecutivo =
                reservaRepository
                        .siguienteValorCodigoReserva();

        if (consecutivo == null
                || consecutivo <= 0) {

            throw new IllegalStateException(
                    "No se pudo generar el codigo de reserva");
        }

        LocalDateTime now =
                LocalDateTime.now();

        String codigoReserva =
                String.format(
                        Locale.ROOT,
                        "BEM-%d-%06d",
                        now.getYear(),
                        consecutivo);

        Reserva reserva = new Reserva();

        reserva.setCodigoReserva(codigoReserva);
        reserva.setCliente(cliente);
        reserva.setSede(sede);
        reserva.setSalon(salon);
        reserva.setPaquete(paquete);
        reserva.setTematica(tematica);
        reserva.setFechaEvento(request.fechaEvento());
        reserva.setHoraInicio(request.horaInicio());
        reserva.setHoraFin(request.horaFin());
        reserva.setCantidadInvitados(
                request.cantidadInvitados());
        reserva.setMontoBase(montoBase);
        reserva.setMontoServicios(montoServicios);
        reserva.setMontoTotal(montoTotal);
        reserva.setPorcentajeDeposito(
                porcentajeDeposito);
        reserva.setMontoDeposito(montoDeposito);
        reserva.setEstado(
                EstadoReserva.PENDIENTE.name());
        reserva.setFechaCreacion(now);
        reserva.setFechaActualizacion(now);

        Reserva reservaGuardada =
                reservaRepository.save(reserva);

        guardarServicios(
                reservaGuardada,
                servicios);

        guardarHistorialCreacion(
                reservaGuardada,
                cliente,
                now);

        return mapResponse(
                reservaGuardada);
    }

    private void validarSalonPerteneceSede(
            Salon salon,
            Sede sede) {

        if (salon.getSede() == null
                || !Objects.equals(
                        salon.getSede().getIdSede(),
                        sede.getIdSede())) {

            throw new IllegalArgumentException(
                    "El salon no pertenece a la sede indicada");
        }

        if (!Boolean.TRUE.equals(
                salon.getSede().getActivo())) {

            throw new RecursoNoEncontradoException(
                    "Sede no encontrada");
        }
    }

    private Paquete buscarPaquete(
            Integer idPaquete) {

        if (idPaquete == null) {
            return null;
        }

        return paqueteRepository
                .findByIdPaqueteAndActivoTrue(
                        idPaquete)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Paquete no encontrado"));
    }

    private Tematica buscarTematica(
            Integer idTematica) {

        if (idTematica == null) {
            return null;
        }

        return tematicaRepository
                .findByIdTematicaAndActivoTrue(
                        idTematica)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Tematica no encontrada"));
    }

    private List<ServicioSeleccionado> buscarServicios(
            List<CrearReservaServicioRequest> requests) {

        if (requests == null
                || requests.isEmpty()) {

            return List.of();
        }

        List<ServicioSeleccionado> resultado =
                new ArrayList<>();

        Set<Integer> ids =
                new HashSet<>();

        for (CrearReservaServicioRequest request : requests) {

            if (request == null) {
                throw new IllegalArgumentException(
                        "Un servicio adicional no puede ser nulo");
            }

            if (request.idServicioAdicional() == null
                    || request.idServicioAdicional() <= 0
                    || request.cantidad() == null
                    || request.cantidad() <= 0) {

                throw new IllegalArgumentException(
                        "El servicio adicional y su cantidad deben ser validos");
            }

            if (!ids.add(
                    request.idServicioAdicional())) {

                throw new IllegalArgumentException(
                        "No se puede repetir un servicio adicional");
            }

            ServicioAdicional servicio =
                    servicioAdicionalRepository
                            .findByIdServicioAdicionalAndActivoTrue(
                                    request.idServicioAdicional())
                            .orElseThrow(() ->
                                    new RecursoNoEncontradoException(
                                            "Servicio adicional no encontrado"));

            if (servicio.getPrecio() == null
                    || servicio.getPrecio().signum() < 0) {

                throw new IllegalStateException(
                        "El servicio adicional tiene un precio invalido");
            }

            BigDecimal subtotal =
                    servicio.getPrecio()
                            .multiply(
                                    BigDecimal.valueOf(
                                            request.cantidad()));

            resultado.add(
                    new ServicioSeleccionado(
                            servicio,
                            request.cantidad(),
                            subtotal));
        }

        return resultado;
    }

    private BigDecimal calcularMontoBase(
            Paquete paquete,
            Tematica tematica) {

        BigDecimal monto =
                BigDecimal.ZERO;

        if (paquete != null) {

            if (paquete.getPrecioBase() == null
                    || paquete.getPrecioBase().signum() < 0) {

                throw new IllegalStateException(
                        "El paquete tiene un precio invalido");
            }

            monto = monto.add(
                    paquete.getPrecioBase());
        }

        if (tematica != null) {

            if (tematica.getPrecioAdicional() == null
                    || tematica.getPrecioAdicional().signum() < 0) {

                throw new IllegalStateException(
                        "La tematica tiene un precio invalido");
            }

            monto = monto.add(
                    tematica.getPrecioAdicional());
        }

        return monto;
    }

    private void guardarServicios(
            Reserva reserva,
            List<ServicioSeleccionado> servicios) {

        if (servicios.isEmpty()) {
            return;
        }

        List<ReservaServicio> detalles =
                servicios
                        .stream()
                        .map(servicio -> {

                            ReservaServicio detalle =
                                    new ReservaServicio();

                            detalle.setReserva(reserva);
                            detalle.setServicioAdicional(
                                    servicio.servicio());
                            detalle.setCantidad(
                                    servicio.cantidad());
                            detalle.setPrecioUnitario(
                                    servicio.servicio().getPrecio());
                            detalle.setSubtotal(
                                    servicio.subtotal());

                            return detalle;
                        })
                        .toList();

        reservaServicioRepository
                .saveAll(detalles);
    }

    private void guardarHistorialCreacion(
            Reserva reserva,
            Cliente cliente,
            LocalDateTime now) {

        HistorialReserva historial =
                new HistorialReserva();

        historial.setReserva(reserva);
        historial.setEstadoAnterior(null);
        historial.setEstadoNuevo(
                EstadoReserva.PENDIENTE.name());
        historial.setObservacion(
                "Reserva creada por el cliente");
        historial.setFechaCambio(now);
        historial.setUsuarioResponsable(
                cliente.getUsuario());

        historialReservaRepository.save(
                historial);
    }

    private ReservaCreadaResponse mapResponse(
            Reserva reserva) {

        return new ReservaCreadaResponse(
                reserva.getIdReserva(),
                reserva.getCodigoReserva(),
                reserva.getEstado(),
                reserva.getSede().getIdSede(),
                reserva.getSalon().getIdSalon(),
                reserva.getPaquete() == null
                        ? null
                        : reserva.getPaquete().getIdPaquete(),
                reserva.getTematica() == null
                        ? null
                        : reserva.getTematica().getIdTematica(),
                reserva.getFechaEvento(),
                reserva.getHoraInicio(),
                reserva.getHoraFin(),
                reserva.getCantidadInvitados(),
                reserva.getMontoBase(),
                reserva.getMontoServicios(),
                reserva.getMontoTotal(),
                reserva.getPorcentajeDeposito(),
                reserva.getMontoDeposito(),
                reserva.getFechaCreacion());
    }

    private record ServicioSeleccionado(
            ServicioAdicional servicio,
            Integer cantidad,
            BigDecimal subtotal) {
    }
}
