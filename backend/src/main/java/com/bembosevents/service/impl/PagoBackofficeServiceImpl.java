package com.bembosevents.service.impl;

import com.bembosevents.dto.pago.PagoResponse;
import com.bembosevents.entity.Pago;
import com.bembosevents.exception.RecursoNoEncontradoException;
import com.bembosevents.repository.PagoRepository;
import com.bembosevents.service.PagoBackofficeService;
import com.bembosevents.service.SedeAccessService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PagoBackofficeServiceImpl
        implements PagoBackofficeService {

    private final PagoRepository pagoRepository;
    private final SedeAccessService sedeAccessService;

    public PagoBackofficeServiceImpl(
            PagoRepository pagoRepository,
            SedeAccessService sedeAccessService) {

        this.pagoRepository =
                pagoRepository;

        this.sedeAccessService =
                sedeAccessService;
    }

    @Override
    public List<PagoResponse> listarAccesibles(
            Authentication authentication) {

        if (sedeAccessService.esAdministrador(
                authentication)) {

            return pagoRepository
                    .findAllByOrderByFechaCreacionDesc()
                    .stream()
                    .map(this::convertir)
                    .toList();
        }

        if (sedeAccessService.esEncargadoLocal(
                authentication)) {

            Integer idSede =
                    sedeAccessService
                            .obtenerSedeAsignada(
                                    authentication.getName());

            return pagoRepository
                    .findByReserva_Sede_IdSedeOrderByFechaCreacionDesc(
                            idSede)
                    .stream()
                    .map(this::convertir)
                    .toList();
        }

        throw new AccessDeniedException(
                "No tiene permisos para consultar pagos BackOffice");
    }

    @Override
    public PagoResponse obtenerAccesible(
            Integer idPago,
            Authentication authentication) {

        if (idPago == null
                || idPago <= 0) {

            throw new IllegalArgumentException(
                    "El id de pago debe ser positivo");
        }

        Pago pago;

        if (sedeAccessService.esAdministrador(
                authentication)) {

            pago =
                    pagoRepository
                            .findById(
                                    idPago)
                            .orElseThrow(() ->
                                    new RecursoNoEncontradoException(
                                            "Pago no encontrado"));
        }
        else if (
                sedeAccessService.esEncargadoLocal(
                        authentication)
        ) {

            Integer idSede =
                    sedeAccessService
                            .obtenerSedeAsignada(
                                    authentication.getName());

            pago =
                    pagoRepository
                            .findByIdPagoAndReserva_Sede_IdSede(
                                    idPago,
                                    idSede)
                            .orElseThrow(() ->
                                    new RecursoNoEncontradoException(
                                            "Pago no encontrado"));
        }
        else {

            throw new AccessDeniedException(
                    "No tiene permisos para consultar pagos BackOffice");
        }

        return convertir(
                pago);
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