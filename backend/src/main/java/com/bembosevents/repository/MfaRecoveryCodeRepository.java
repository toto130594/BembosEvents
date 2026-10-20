package com.bembosevents.repository;

import com.bembosevents.entity.MfaRecoveryCode;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MfaRecoveryCodeRepository
        extends JpaRepository<MfaRecoveryCode, Long> {

    Optional<MfaRecoveryCode> findByCodigoHash(
            String codigoHash
    );

    List<MfaRecoveryCode>
    findAllByUsuario_IdUsuarioAndFechaUsoIsNullOrderByIdRecoveryCodeAsc(
            Integer idUsuario
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select rc
            from MfaRecoveryCode rc
            join fetch rc.usuario
            where rc.usuario.idUsuario = :idUsuario
              and rc.fechaUso is null
            order by rc.idRecoveryCode asc
            """)
    List<MfaRecoveryCode> findAllUnusedForUpdate(
            @Param("idUsuario") Integer idUsuario
    );
}