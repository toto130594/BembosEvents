package com.bembosevents.service.impl;

import com.bembosevents.domain.EstadoPago;
import com.bembosevents.domain.ReglasTransicionPago;
import com.bembosevents.dto.pago.CrearPagoRequest;
import com.bembosevents.dto.pago.PagoResponse;
import com.bembosevents.entity.Cliente;
import com.bembosevents.entity.Pago;
import com.bembosevents.entity.Reserva;
import com.bembosevents.exception.RecursoNoEncontradoException;
import com.bembosevents.repository.ClienteRepository;
import com.bembosevents.repository.PagoRepository;
import com.bembosevents.repository.ReservaRepository;
import com.bembosevents.service.PagoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PagoServiceImpl implements PagoService {

    private final PagoRepository pagoRepository;
    private final ReservaRepository reservaRepository;
    private final ClienteRepository clienteRepository;

    public PagoServiceImpl(
            PagoRepository pagoRepository,
            ReservaRepository reservaRepository,
            ClienteRepository clienteRepository) {

        this.pagoRepository = pagoRepository;
        this.reservaRepository = reservaRepository;
        this.clienteRepository = clienteRepository;
    }

    @Override
    @Transactional
    public PagoResponse iniciarPago(
            String correoCliente,
            Integer idReserva,
            CrearPagoRequest request) {

        validarCorreo(
                correoCliente);

        validarIdReserva(
                idReserva);

        if (request == null) {
            throw new IllegalArgumentException(
                    "Los datos del pago son obligatorios");
        }

        BigDecimal monto =
                normalizarMonto(
                        request.monto());

        String metodoPago =
                normalizarMetodoPago(
                        request.metodoPago());

        Reserva reserva =
                buscarReservaPropia(
                        correoCliente,
                        idReserva);

        LocalDateTime ahora =
                LocalDateTime.now();

        Pago pago =
                new Pago();

        pago.setReserva(
                reserva);

        pago.setMonto(
                monto);

        pago.setMetodoPago(
                metodoPago);

        pago.setEstadoPago(
                EstadoPago.PENDIENTE.name());

        pago.setReferenciaExterna(
                null);

        pago.setFechaPago(
                null);

        pago.setFechaCreacion(
                ahora);

        Pago pagoGuardado =
                pagoRepository.save(
                        pago);

        return convertir(
                pagoGuardado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoResponse> listarPagosPropios(
            String correoCliente,
            Integer idReserva) {

        validarCorreo(
                correoCliente);

        validarIdReserva(
                idReserva);

        buscarReservaPropia(
                correoCliente,
                idReserva);

        return pagoRepository
                .findByReserva_IdReservaOrderByFechaCreacionDesc(
                        idReserva)
                .stream()
                .map(this::convertir)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PagoResponse obtenerPagoPropio(
            String correoCliente,
            Integer idPago) {

        validarCorreo(
                correoCliente);

        validarIdPago(
                idPago);

        Cliente cliente =
                clienteRepository
                        .findByUsuario_CorreoIgnoreCaseAndActivoTrue(
                                correoCliente.strip())
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "El usuario autenticado no tiene un perfil Cliente activo"));

        Pago pago =
                pagoRepository
                        .findByIdPagoAndReserva_Cliente_IdCliente(
                                idPago,
                                cliente.getIdCliente())
                        .orElseThrow(() ->
                                new RecursoNoEncontradoException(
                                        "Pago no encontrado"));

        return convertir(
                pago);
    }

    @Override
    @Transactional
    public PagoResponse vincularReferenciaExterna(
            Integer idPago,
            String referenciaExterna) {

        validarIdPago(idPago);

        if (referenciaExterna == null
                || referenciaExterna.isBlank()) {

            throw new IllegalArgumentException(
                    "La referencia externa es obligatoria");
        }

        String referenciaNormalizada =
                referenciaExterna.trim();

        if (referenciaNormalizada.length() > 150) {
            throw new IllegalArgumentException(
                    "La referencia externa no puede superar 150 caracteres");
        }

        Pago pago =
                pagoRepository
                        .findById(idPago)
                        .orElseThrow(() ->
                                new RecursoNoEncontradoException(
                                        "Pago no encontrado"));

        if (pago.getReferenciaExterna() != null
                && !pago.getReferenciaExterna().isBlank()
                && !pago.getReferenciaExterna()
                        .equals(referenciaNormalizada)) {

            throw new IllegalStateException(
                    "El pago ya tiene una referencia externa diferente");
        }

        pago.setReferenciaExterna(
                referenciaNormalizada);

        return convertir(
                pagoRepository.save(pago));
    }

    @Override
    @Transactional
    public PagoResponse actualizarEstadoDesdeProveedor(
            String referenciaExterna,
            EstadoPago nuevoEstado) {

        if (referenciaExterna == null
                || referenciaExterna.isBlank()) {

            throw new IllegalArgumentException(
                    "La referencia externa es obligatoria");
        }

        if (nuevoEstado == null) {
            throw new IllegalArgumentException(
                    "El nuevo estado es obligatorio");
        }

        Pago pago =
                pagoRepository
                        .findByReferenciaExterna(
                                referenciaExterna.trim())
                        .orElseThrow(() ->
                                new RecursoNoEncontradoException(
                                        "Pago Mercado Pago no encontrado"));

        EstadoPago estadoActual =
                EstadoPago.valueOf(
                        pago.getEstadoPago());

        if (estadoActual == nuevoEstado) {
            return convertir(
                    pago);
        }

        validarTransicionEstado(
                estadoActual,
                nuevoEstado);

        pago.setEstadoPago(
                nuevoEstado.name());

        if (nuevoEstado == EstadoPago.APROBADO
                && pago.getFechaPago() == null) {

            pago.setFechaPago(
                    LocalDateTime.now());
        }

        return convertir(
                pagoRepository.save(
                        pago));
    }
    @Override
    public void validarTransicionEstado(
            EstadoPago estadoActual,
            EstadoPago nuevoEstado) {

        if (!ReglasTransicionPago.esValida(
                estadoActual,
                nuevoEstado)) {

            throw new IllegalArgumentException(
                    "Transicion de estado de pago no permitida");
        }
    }

    private Reserva buscarReservaPropia(
            String correoCliente,
            Integer idReserva) {

        Cliente cliente =
                clienteRepository
                        .findByUsuario_CorreoIgnoreCaseAndActivoTrue(
                                correoCliente.strip())
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "El usuario autenticado no tiene un perfil Cliente activo"));

        return reservaRepository
                .findByIdReservaAndCliente_IdCliente(
                        idReserva,
                        cliente.getIdCliente())
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Reserva no encontrada"));
    }

    private void validarCorreo(
            String correoCliente) {

        if (correoCliente == null
                || correoCliente.isBlank()) {

            throw new IllegalArgumentException(
                    "El cliente autenticado es obligatorio");
        }
    }

    private void validarIdReserva(
            Integer idReserva) {

        if (idReserva == null
                || idReserva <= 0) {

            throw new IllegalArgumentException(
                    "El id de reserva debe ser positivo");
        }
    }

    private void validarIdPago(
            Integer idPago) {

        if (idPago == null
                || idPago <= 0) {

            throw new IllegalArgumentException(
                    "El id de pago debe ser positivo");
        }
    }

    private BigDecimal normalizarMonto(
            BigDecimal monto) {

        if (monto == null) {
            throw new IllegalArgumentException(
                    "El monto es obligatorio");
        }

        if (monto.signum() <= 0) {
            throw new IllegalArgumentException(
                    "El monto debe ser mayor que 0");
        }

        BigDecimal sinCeros =
                monto.stripTrailingZeros();

        if (sinCeros.scale() > 2) {
            throw new IllegalArgumentException(
                    "El monto debe tener maximo 2 decimales");
        }

        if (sinCeros.precision()
                - Math.max(
                        sinCeros.scale(),
                        0) > 10) {

            throw new IllegalArgumentException(
                    "El monto supera el limite permitido");
        }

        return monto.setScale(2);
    }

    private String normalizarMetodoPago(
            String metodoPago) {

        if (metodoPago == null
                || metodoPago.isBlank()) {

            throw new IllegalArgumentException(
                    "El metodo de pago es obligatorio");
        }

        String normalizado =
                metodoPago.strip();

        if (normalizado.length() > 50) {
            throw new IllegalArgumentException(
                    "El metodo de pago no puede superar 50 caracteres");
        }

        return normalizado;
    }

    private PagoResponse convertir(
            Pago pago) {

        return new PagoResponse(
                pago.getIdPago(),
                pago.getReserva().getIdReserva(),
                pago.getMonto(),
                pago.getMetodoPago(),
                pago.getEstadoPago(),
                pago.getReferenciaExterna(),
                pago.getFechaPago(),
                pago.getFechaCreacion());
    }
}