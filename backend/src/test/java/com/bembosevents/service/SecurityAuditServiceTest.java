package com.bembosevents.service;

import com.bembosevents.domain.TipoEventoSeguridad;
import com.bembosevents.entity.AuditoriaSeguridad;
import com.bembosevents.entity.Usuario;
import com.bembosevents.repository.AuditoriaSeguridadRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SecurityAuditServiceTest {

    @Mock
    private AuditoriaSeguridadRepository
            auditoriaSeguridadRepository;

    @Test
    void debeRegistrarEventoSeguro() {

        SecurityAuditService service =
                new SecurityAuditService(
                        auditoriaSeguridadRepository);

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(10);

        service.record(
                usuario,
                TipoEventoSeguridad.LOGIN_EXITOSO,
                true,
                "Login correcto");

        ArgumentCaptor<AuditoriaSeguridad> captor =
                ArgumentCaptor.forClass(
                        AuditoriaSeguridad.class);

        verify(auditoriaSeguridadRepository)
                .save(captor.capture());

        AuditoriaSeguridad auditoria =
                captor.getValue();

        assertEquals(
                usuario,
                auditoria.getUsuario());

        assertEquals(
                TipoEventoSeguridad.LOGIN_EXITOSO,
                auditoria.getTipoEvento());

        assertEquals(
                true,
                auditoria.getExitoso());

        assertEquals(
                "Login correcto",
                auditoria.getDetalleSeguro());
    }

    @Test
    void debeSanitizarDetalle() {

        SecurityAuditService service =
                new SecurityAuditService(
                        auditoriaSeguridadRepository);

        String detalle =
                "x".repeat(1100)
                        + "\n"
                        + "linea";

        service.record(
                null,
                TipoEventoSeguridad.LOGIN_FALLIDO,
                false,
                detalle);

        ArgumentCaptor<AuditoriaSeguridad> captor =
                ArgumentCaptor.forClass(
                        AuditoriaSeguridad.class);

        verify(auditoriaSeguridadRepository)
                .save(captor.capture());

        String persistido =
                captor.getValue()
                        .getDetalleSeguro();

        assertEquals(
                1000,
                persistido.length());

        assertFalse(
                persistido.contains("\n"));

        assertFalse(
                persistido.contains("\r"));
    }
}
