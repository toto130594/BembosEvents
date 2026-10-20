package com.bembosevents.security.mfa;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MfaSecretProtectorTest {

    private static final String RAW_SECRET =
            "JBSWY3DPEHPK3PXP";

    @Test
    void debeProtegerYRecuperarSecreto() {

        MfaSecretProtector protector =
                new MfaSecretProtector(
                        key(
                                "12345678901234567890123456789012"));

        String protectedValue =
                protector.protect(
                        RAW_SECRET);

        assertNotEquals(
                RAW_SECRET,
                protectedValue);

        assertFalse(
                protectedValue.contains(
                        RAW_SECRET));

        assertTrue(
                protectedValue.startsWith(
                        "v1."));

        assertEquals(
                RAW_SECRET,
                protector.unprotect(
                        protectedValue));
    }

    @Test
    void cifradosDelMismoSecretoSonDistintos() {

        MfaSecretProtector protector =
                new MfaSecretProtector(
                        key(
                                "12345678901234567890123456789012"));

        String first =
                protector.protect(
                        RAW_SECRET);

        String second =
                protector.protect(
                        RAW_SECRET);

        assertNotEquals(
                first,
                second);
    }

    @Test
    void payloadAlteradoEsRechazado() {

        MfaSecretProtector protector =
                new MfaSecretProtector(
                        key(
                                "12345678901234567890123456789012"));

        String protectedValue =
                protector.protect(
                        RAW_SECRET);

        String encodedPayload =
                protectedValue.substring(
                        "v1.".length());

        byte[] tamperedPayload =
                Base64
                        .getUrlDecoder()
                        .decode(
                                encodedPayload);

        /*
         * Se modifica un byte REAL del payload binario.
         *
         * No se altera solamente un caracter Base64URL porque
         * los bits de padding implicitos pueden producir los
         * mismos bytes decodificados y volver el test flaky.
         *
         * El ultimo byte pertenece al ciphertext/tag GCM.
         */
        tamperedPayload[
                tamperedPayload.length - 1] ^= 0x01;

        String tampered =
                "v1."
                        + Base64
                        .getUrlEncoder()
                        .withoutPadding()
                        .encodeToString(
                                tamperedPayload);

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        protector.unprotect(
                                tampered));
    }

    @Test
    void claveIncorrectaNoPuedeDescifrar() {

        MfaSecretProtector first =
                new MfaSecretProtector(
                        key(
                                "12345678901234567890123456789012"));

        MfaSecretProtector second =
                new MfaSecretProtector(
                        key(
                                "abcdefghijklmnopqrstuvwxyz123456"));

        String protectedValue =
                first.protect(
                        RAW_SECRET);

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        second.unprotect(
                                protectedValue));
    }

    @Test
    void sinClaveNoPermiteOperaciones() {

        MfaSecretProtector protector =
                new MfaSecretProtector(
                        "");

        assertFalse(
                protector.isConfigured());

        assertThrows(
                IllegalStateException.class,
                () ->
                        protector.protect(
                                RAW_SECRET));
    }

    private String key(
            String value) {

        return Base64
                .getEncoder()
                .encodeToString(
                        value.getBytes(
                                StandardCharsets.UTF_8));
    }
}
