package com.bembosevents.repository;

import com.bembosevents.entity.SesionUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
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

    @Modifying
    @Query("""
            update SesionUsuario s
            set s.fechaRevocacion = :fechaRevocacion
            where s.usuario.idUsuario = :idUsuario
              and s.fechaRevocacion is null
            """)
    int revokeActiveSessions(
            @Param("idUsuario") Integer idUsuario,
            @Param("fechaRevocacion") LocalDateTime fechaRevocacion
    );
}
