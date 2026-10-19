package com.bembosevents.repository;

import com.bembosevents.entity.MfaRecoveryCode;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
