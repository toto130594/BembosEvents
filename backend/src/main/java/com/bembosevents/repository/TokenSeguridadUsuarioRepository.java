package com.bembosevents.repository;

import com.bembosevents.domain.TipoTokenSeguridad;
import com.bembosevents.entity.TokenSeguridadUsuario;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TokenSeguridadUsuarioRepository
        extends JpaRepository<TokenSeguridadUsuario, Integer> {

    Optional<TokenSeguridadUsuario> findByTokenHashAndTipo(
            String tokenHash,
            TipoTokenSeguridad tipo
    );

    List<TokenSeguridadUsuario>
    findAllByUsuario_IdUsuarioAndTipoAndFechaUsoIsNullAndFechaRevocacionIsNull(
            Integer idUsuario,
            TipoTokenSeguridad tipo
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select t
            from TokenSeguridadUsuario t
            join fetch t.usuario
            where t.tokenHash = :tokenHash
              and t.tipo = :tipo
            """)
    Optional<TokenSeguridadUsuario> findForUpdate(
            @Param("tokenHash") String tokenHash,
            @Param("tipo") TipoTokenSeguridad tipo
    );

    @Modifying
    @Query("""
            update TokenSeguridadUsuario t
            set t.fechaRevocacion = :fechaRevocacion
            where t.usuario.idUsuario = :idUsuario
              and t.tipo = :tipo
              and t.fechaUso is null
              and t.fechaRevocacion is null
            """)
    int revokeActiveTokens(
            @Param("idUsuario") Integer idUsuario,
            @Param("tipo") TipoTokenSeguridad tipo,
            @Param("fechaRevocacion") LocalDateTime fechaRevocacion
    );
}
