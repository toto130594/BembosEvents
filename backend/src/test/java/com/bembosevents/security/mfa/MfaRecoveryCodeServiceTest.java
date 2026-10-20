package com.bembosevents.security.mfa;

import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MfaRecoveryCodeServiceTest {

    @Test
    void generaOchoCodigosUnicos() {

        MfaRecoveryCodeService service =
                service();

        List<MfaRecoveryCodeMaterial> codes =
                service.generateBatch();

        assertEquals(
                8,
                codes.size());

        Set<String> raw =
                codes.stream()
                        .map(
                                MfaRecoveryCodeMaterial::rawCode)
                        .collect(
                                Collectors.toSet());

        assertEquals(
                8,
                raw.size());
    }

    @Test
    void rawCodeNoEsIgualAlHash() {

        MfaRecoveryCodeService service =
                service();

        MfaRecoveryCodeMaterial code =
                service.generateBatch()
                        .get(0);

        assertNotEquals(
                code.rawCode(),
                code.codeHash());

        assertTrue(
                code.codeHash()
                        .matches(
                                "[0-9a-f]{64}"));
    }

    @Test
    void comparaCodigoEnTiempoConstante() {

        MfaRecoveryCodeService service =
                service();

        MfaRecoveryCodeMaterial code =
                service.generateBatch()
                        .get(0);

        assertTrue(
                service.matches(
                        code.rawCode(),
                        code.codeHash()));

        assertFalse(
                service.matches(
                        "AAAA-BBBB-CCCC-DDDD",
                        code.codeHash()));
    }

    @Test
    void normalizacionToleraGuionesYMinusculas() {

        MfaRecoveryCodeService service =
                service();

        MfaRecoveryCodeMaterial code =
                service.generateBatch()
                        .get(0);

        String compact =
                code.rawCode()
                        .replace(
                                "-",
                                "")
                        .toLowerCase();

        assertTrue(
                service.matches(
                        compact,
                        code.codeHash()));
    }

    private MfaRecoveryCodeService service() {

        return new MfaRecoveryCodeService(
                new SecureRandom(),
                8,
                10);
    }
}
