package com.bembosevents.security.mfa;

import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TotpServiceTest {

    private static final String RFC_SECRET =
            "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ";

    @Test
    void vectorConocidoHotpCounterUno() {

        TotpService service =
                service(
                        0);

        String code =
                service.generateCode(
                        RFC_SECRET,
                        Instant.ofEpochSecond(
                                59));

        assertEquals(
                "287082",
                code);

        assertTrue(
                service.verify(
                        RFC_SECRET,
                        "287082",
                        Instant.ofEpochSecond(
                                59)));
    }

    @Test
    void ventanaUnoAceptaPasoAnteriorYPosterior() {

        TotpService service =
                service(
                        1);

        Instant current =
                Instant.ofEpochSecond(
                        60);

        assertTrue(
                service.verify(
                        RFC_SECRET,
                        "287082",
                        current));

        assertTrue(
                service.verify(
                        RFC_SECRET,
                        "359152",
                        current));

        assertTrue(
                service.verify(
                        RFC_SECRET,
                        "969429",
                        current));
    }

    @Test
    void codigoFueraDeVentanaEsRechazado() {

        TotpService service =
                service(
                        1);

        assertFalse(
                service.verify(
                        RFC_SECRET,
                        "338314",
                        Instant.ofEpochSecond(
                                60)));
    }

    @Test
    void generaSecretoCon160Bits() {

        TotpService service =
                service(
                        1);

        String secret =
                service.generateSecret();

        assertEquals(
                20,
                Base32Codec
                        .decode(
                                secret)
                        .length);
    }

    @Test
    void generaUriOtpAuthSinExponerNadaExtra() {

        TotpService service =
                service(
                        1);

        String uri =
                service.buildOtpAuthUri(
                        "cliente@bembos.test",
                        RFC_SECRET);

        assertTrue(
                uri.startsWith(
                        "otpauth://totp/"));

        assertTrue(
                uri.contains(
                        "secret="
                                + RFC_SECRET));

        assertTrue(
                uri.contains(
                        "issuer=Bembos%20Events"));

        assertTrue(
                uri.contains(
                        "algorithm=SHA1"));

        assertTrue(
                uri.contains(
                        "digits=6"));

        assertTrue(
                uri.contains(
                        "period=30"));
    }

    @Test
    void formatoCodigoInvalidoRetornaFalse() {

        TotpService service =
                service(
                        1);

        assertFalse(
                service.verify(
                        RFC_SECRET,
                        "ABC123",
                        Instant.ofEpochSecond(
                                59)));

        assertFalse(
                service.verify(
                        RFC_SECRET,
                        "12345",
                        Instant.ofEpochSecond(
                                59)));
    }

    private TotpService service(
            int window) {

        return new TotpService(
                new SecureRandom(),
                20,
                6,
                30,
                window,
                "Bembos Events");
    }
}
