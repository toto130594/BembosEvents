package com.bembosevents.repository;

import com.bembosevents.entity.UsuarioSede;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioSedeRepository
        extends JpaRepository<UsuarioSede, Integer> {

    Optional<UsuarioSede> findByUsuario_IdUsuario(Integer idUsuario);

    Optional<UsuarioSede> findByUsuario_IdUsuarioAndActivoTrue(
            Integer idUsuario
    );

    Optional<UsuarioSede> findByUsuario_CorreoIgnoreCaseAndActivoTrue(
            String correo
    );

    boolean existsByUsuario_CorreoIgnoreCaseAndSede_IdSedeAndActivoTrue(
            String correo,
            Integer idSede
    );

    List<UsuarioSede> findBySede_IdSedeAndActivoTrue(
            Integer idSede
    );
}