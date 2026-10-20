package com.bembosevents.service;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RefreshReuseTransactionBoundaryTest {

    @Test
    void debeUsarUnaSolaTransaccionCommitSafeParaReuse()
            throws Exception {

        Method rotar =
                SesionUsuarioService.class
                        .getMethod(
                                "rotar",
                                String.class);

        Transactional rotarTx =
                rotar.getAnnotation(
                        Transactional.class);

        assertNotNull(
                rotarTx);

        assertTrue(
                Arrays.asList(
                        rotarTx.noRollbackFor())
                        .contains(
                                BadCredentialsException.class));

        Method refresh =
                AuthSessionIssuanceService.class
                        .getMethod(
                                "refresh",
                                String.class);

        Transactional refreshTx =
                refresh.getAnnotation(
                        Transactional.class);

        assertNotNull(
                refreshTx);

        assertTrue(
                Arrays.asList(
                        refreshTx.noRollbackFor())
                        .contains(
                                BadCredentialsException.class));

        Method reuse =
                RefreshTokenReuseService.class
                        .getMethod(
                                "detectAndRevokeIfReused",
                                String.class);

        Transactional reuseTx =
                reuse.getAnnotation(
                        Transactional.class);

        assertNotNull(
                reuseTx);

        assertEquals(
                Propagation.MANDATORY,
                reuseTx.propagation());
    }
}