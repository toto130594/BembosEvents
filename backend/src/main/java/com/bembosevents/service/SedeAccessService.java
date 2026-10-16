package com.bembosevents.service;

import com.bembosevents.entity.UsuarioSede;
import com.bembosevents.repository.UsuarioSedeRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class SedeAccessService {

    private static final String ROLE_ADMINISTRADOR =
            "ROLE_ADMINISTRADOR";

    private static final String ROLE_ENCARGADO_LOCAL =
            "ROLE_ENCARGADO_LOCAL";

    private final UsuarioSedeRepository usuarioSedeRepository;

    public SedeAccessService(
            UsuarioSedeRepository usuarioSedeRepository) {

        this.usuarioSedeRepository = usuarioSedeRepository;
    }

    public boolean esAdministrador(
            Authentication authentication) {

        return tieneRol(
                authentication,
                ROLE_ADMINISTRADOR
        );
    }

    public boolean esEncargadoLocal(
            Authentication authentication) {

        return tieneRol(
                authentication,
                ROLE_ENCARGADO_LOCAL
        );
    }

    public boolean puedeAccederSede(
            Authentication authentication,
            Integer idSede) {

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            return false;
        }

        if (esAdministrador(authentication)) {
            return true;
        }

        if (!esEncargadoLocal(authentication)) {
            return false;
        }

        return usuarioSedeRepository
                .existsByUsuario_CorreoIgnoreCaseAndSede_IdSedeAndActivoTrue(
                        authentication.getName(),
                        idSede
                );
    }

    public void validarAccesoSede(
            Authentication authentication,
            Integer idSede) {

        if (!puedeAccederSede(
                authentication,
                idSede
        )) {

            throw new AccessDeniedException(
                    "No tiene permisos para acceder a esta sede"
            );
        }
    }

    public Integer obtenerSedeAsignada(
            String correo) {

        UsuarioSede asignacion =
                usuarioSedeRepository
                        .findByUsuario_CorreoIgnoreCaseAndActivoTrue(
                                correo
                        )
                        .orElseThrow(() ->
                                new AccessDeniedException(
                                        "El usuario no tiene una sede asignada"
                                )
                        );

        return asignacion
                .getSede()
                .getIdSede();
    }

    private boolean tieneRol(
            Authentication authentication,
            String rol) {

        if (authentication == null) {
            return false;
        }

        return authentication
                .getAuthorities()
                .stream()
                .anyMatch(authority ->
                        rol.equals(
                                authority.getAuthority()
                        )
                );
    }
}