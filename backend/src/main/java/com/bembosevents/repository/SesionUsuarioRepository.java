package com.bembosevents.repository;

import com.bembosevents.entity.SesionUsuario;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
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

    Optional<SesionUsuario>
    findByRefreshTokenHashAndFechaRevocacionIsNull(
            String refreshTokenHash
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select s
            from SesionUsuario s
            join fetch s.usuario
            where s.refreshTokenHash = :refreshTokenHash
            """)
    Optional<SesionUsuario> findByRefreshTokenHashForUpdate(
            @Param("refreshTokenHash") String refreshTokenHash
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select s
            from SesionUsuario s
            join fetch s.usuario
            where s.idSesionUsuario = :idSesionUsuario
            """)
    Optional<SesionUsuario> findByIdSesionUsuarioForUpdate(
            @Param("idSesionUsuario")
            Long idSesionUsuario
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

    List<SesionUsuario>
    findAllByUsuario_IdUsuarioAndFechaRevocacionIsNullAndFechaExpiracionAfterOrderByFechaCreacionDesc(
            Integer idUsuario,
            LocalDateTime fechaActual);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select s
            from SesionUsuario s
            join fetch s.usuario
            where s.idSesionUsuario = :idSesionUsuario
              and s.usuario.idUsuario = :idUsuario
              and s.fechaRevocacion is null
              and s.fechaExpiracion > :fechaActual
            """)
    Optional<SesionUsuario> findActiveOwnedByIdForUpdate(
            @Param("idSesionUsuario") Long idSesionUsuario,
            @Param("idUsuario") Integer idUsuario,
            @Param("fechaActual") LocalDateTime fechaActual);
}
