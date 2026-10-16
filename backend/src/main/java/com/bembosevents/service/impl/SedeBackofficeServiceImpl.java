package com.bembosevents.service.impl;

import com.bembosevents.dto.sede.SedeResumenResponse;
import com.bembosevents.entity.Sede;
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
                                    sede.getActivo()
                            )
                    )
                    .map(this::convertir)
                    .toList();
        }

        if (sedeAccessService
                .esEncargadoLocal(authentication)) {

            Integer idSede =
                    sedeAccessService
                            .obtenerSedeAsignada(
                                    authentication.getName()
                            );

            return List.of(
                    obtenerSedeActiva(idSede)
            );
        }

        throw new AccessDeniedException(
                "No tiene permisos para acceder al BackOffice"
        );
    }

    @Override
    public SedeResumenResponse obtenerAccesible(
            Integer idSede,
            Authentication authentication) {

        sedeAccessService.validarAccesoSede(
                authentication,
                idSede
        );

        return obtenerSedeActiva(idSede);
    }

    private SedeResumenResponse obtenerSedeActiva(
            Integer idSede) {

        Sede sede = sedeRepository
                .findById(idSede)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Sede no encontrada"
                        )
                );

        if (!Boolean.TRUE.equals(
                sede.getActivo()
        )) {

            throw new IllegalArgumentException(
                    "La sede se encuentra inactiva"
            );
        }

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
                sede.getActivo()
        );
    }
}