package com.bembosevents.repository;

import com.bembosevents.entity.UsuarioSeguridad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioSeguridadRepository
        extends JpaRepository<UsuarioSeguridad, Integer> {

    Optional<UsuarioSeguridad> findByUsuario_CorreoIgnoreCase(
            String correo
    );
}
