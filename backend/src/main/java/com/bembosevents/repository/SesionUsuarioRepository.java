package com.bembosevents.repository;

import com.bembosevents.entity.SesionUsuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SesionUsuarioRepository
        extends JpaRepository<SesionUsuario, Long> {

    Optional<SesionUsuario> findByRefreshTokenHash(
            String refreshTokenHash
    );

    Optional<SesionUsuario> findByRefreshTokenHashAndFechaRevocacionIsNull(
            String refreshTokenHash
    );

    List<SesionUsuario>
    findAllByUsuario_IdUsuarioAndFechaRevocacionIsNullOrderByFechaCreacionDesc(
            Integer idUsuario
    );
}
