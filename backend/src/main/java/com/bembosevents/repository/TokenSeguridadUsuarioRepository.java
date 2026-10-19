package com.bembosevents.repository;

import com.bembosevents.domain.TipoTokenSeguridad;
import com.bembosevents.entity.TokenSeguridadUsuario;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
