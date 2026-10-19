package com.bembosevents.repository;

import com.bembosevents.entity.AuditoriaSeguridad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditoriaSeguridadRepository
        extends JpaRepository<AuditoriaSeguridad, Long> {

    List<AuditoriaSeguridad>
    findAllByUsuario_IdUsuarioOrderByFechaEventoDesc(
            Integer idUsuario
    );
}
