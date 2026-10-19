package com.bembosevents.repository;

import com.bembosevents.entity.UsuarioSeguridad;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UsuarioSeguridadRepository
        extends JpaRepository<UsuarioSeguridad, Integer> {

    Optional<UsuarioSeguridad> findByUsuario_CorreoIgnoreCase(
            String correo
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select us
            from UsuarioSeguridad us
            where us.idUsuario = :idUsuario
            """)
    Optional<UsuarioSeguridad> findByIdUsuarioForUpdate(
            @Param("idUsuario") Integer idUsuario
    );
}
