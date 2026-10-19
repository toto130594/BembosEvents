package com.bembosevents.service;

import com.bembosevents.domain.TipoEventoSeguridad;
import com.bembosevents.entity.AuditoriaSeguridad;
import com.bembosevents.entity.Usuario;
import com.bembosevents.repository.AuditoriaSeguridadRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SecurityAuditService {

    private static final int MAX_DETALLE_LENGTH = 1000;

    private final AuditoriaSeguridadRepository auditoriaSeguridadRepository;

    public SecurityAuditService(
            AuditoriaSeguridadRepository auditoriaSeguridadRepository) {

        this.auditoriaSeguridadRepository =
                auditoriaSeguridadRepository;
    }

    @Transactional
    public void record(
            Usuario usuario,
            TipoEventoSeguridad tipoEvento,
            boolean exitoso,
            String detalleSeguro) {

        AuditoriaSeguridad auditoria =
                new AuditoriaSeguridad();

        auditoria.setUsuario(usuario);
        auditoria.setTipoEvento(tipoEvento);
        auditoria.setExitoso(exitoso);
        auditoria.setDetalleSeguro(
                sanitizeDetail(detalleSeguro));

        auditoriaSeguridadRepository.save(
                auditoria);
    }

    private String sanitizeDetail(
            String detalleSeguro) {

        if (detalleSeguro == null) {
            return null;
        }

        String sanitized =
                detalleSeguro
                        .replace('\r', ' ')
                        .replace('\n', ' ')
                        .trim();

        if (sanitized.length()
                <= MAX_DETALLE_LENGTH) {
            return sanitized;
        }

        return sanitized.substring(
                0,
                MAX_DETALLE_LENGTH);
    }
}
