package com.bembosevents.service.impl;

import com.bembosevents.dto.sede.SedeResumenResponse;
import com.bembosevents.entity.Sede;
import com.bembosevents.exception.RecursoNoEncontradoException;
import com.bembosevents.repository.SedeRepository;
import com.bembosevents.service.SedeAccessService;
import com.bembosevents.service.SedeBackofficeService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class SedeBackofficeServiceImpl
        implements SedeBackofficeService {

    private final SedeRepository sedeRepository;
    private final SedeAccessService sedeAccessService;

    public SedeBackofficeServiceImpl(
            SedeRepository sedeRepository,
            SedeAccessService sedeAccessService) {

        this.sedeRepository = sedeRepository;
        this.sedeAccessService = sedeAccessService;
    }

    @Override
    public List<SedeResumenResponse> listarAccesibles(
            Authentication authentication) {

        if (sedeAccessService
                .esAdministrador(authentication)) {

            return sedeRepository
                    .findAll()
                    .stream()
                    .filter(sede ->
                            Boolean.TRUE.equals(
                                    sede.getActivo()))
                    .map(this::convertir)
                    .toList();
        }

        if (sedeAccessService
                .esEncargadoLocal(authentication)) {

            Integer idSede =
                    sedeAccessService
                            .obtenerSedeAsignada(
                                    authentication.getName());

            return List.of(
                    obtenerSedeActiva(idSede));
        }

        throw new AccessDeniedException(
                "No tiene permisos para acceder al BackOffice");
    }

    @Override
    public SedeResumenResponse obtenerAccesible(
            Integer idSede,
            Authentication authentication) {

        validarIdSede(idSede);

        if (sedeAccessService
                .esAdministrador(authentication)) {

            return obtenerSedeActiva(idSede);
        }

        if (sedeAccessService
                .esEncargadoLocal(authentication)) {

            Integer idSedeAsignada =
                    sedeAccessService
                            .obtenerSedeAsignada(
                                    authentication.getName());

            if (!idSedeAsignada.equals(idSede)) {

                throw new RecursoNoEncontradoException(
                        "Sede no encontrada");
            }

            return obtenerSedeActiva(idSede);
        }

        throw new AccessDeniedException(
                "No tiene permisos para acceder al BackOffice");
    }

    private void validarIdSede(
            Integer idSede) {

        if (idSede == null ||
                idSede <= 0) {

            throw new IllegalArgumentException(
                    "El id de sede debe ser positivo");
        }
    }

    private SedeResumenResponse obtenerSedeActiva(
            Integer idSede) {

        Sede sede = sedeRepository
                .findByIdSedeAndActivoTrue(idSede)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Sede no encontrada"));

        return convertir(sede);
    }

    private SedeResumenResponse convertir(
            Sede sede) {

        return new SedeResumenResponse(
                sede.getIdSede(),
                sede.getNombre(),
                sede.getDireccion(),
                sede.getDistrito(),
                sede.getTelefono(),
                sede.getActivo());
    }
}
