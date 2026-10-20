package com.bembosevents.repository;

import jakarta.persistence.LockModeType;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SesionUsuarioRepositoryContractTest {

    @Test
    void refreshLookupDebeUsarPessimisticWrite()
            throws Exception {

        Method method =
                SesionUsuarioRepository.class
                        .getMethod(
                                "findByRefreshTokenHashForUpdate",
                                String.class);

        Lock lock =
                method.getAnnotation(
                        Lock.class);

        assertNotNull(
                lock);

        assertEquals(
                LockModeType.PESSIMISTIC_WRITE,
                lock.value());

        Query query =
                method.getAnnotation(
                        Query.class);

        assertNotNull(
                query);

        assertTrue(
                query.value()
                        .contains(
                                "refreshTokenHash"));
    }
}