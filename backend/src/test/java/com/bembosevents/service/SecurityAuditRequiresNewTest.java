package com.bembosevents.service;

import com.bembosevents.domain.TipoEventoSeguridad;
import com.bembosevents.entity.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SecurityAuditRequiresNewTest {

    @Test
    void failureAuditDebePoderPersistirEnTransaccionIndependiente()
            throws Exception {

        Method method =
                SecurityAuditService.class
                        .getMethod(
                                "recordRequiresNew",
                                Usuario.class,
                                TipoEventoSeguridad.class,
                                boolean.class,
                                String.class);

        Transactional transactional =
                method.getAnnotation(
                        Transactional.class);

        assertNotNull(
                transactional);

        assertEquals(
                Propagation.REQUIRES_NEW,
                transactional.propagation());
    }
}