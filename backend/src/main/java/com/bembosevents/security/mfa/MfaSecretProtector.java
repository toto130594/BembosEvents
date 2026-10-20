package com.bembosevents.security.mfa;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Service
public class MfaSecretProtector {

    private static final String VERSION_PREFIX =
            "v1.";

    private static final String TRANSFORMATION =
            "AES/GCM/NoPadding";

    private static final int KEY_BYTES =
            32;

    private static final int IV_BYTES =
            12;

    private static final int TAG_BITS =
            128;

    private static final byte[] AAD =
            "bembosevents:mfa:totp:v1"
                    .getBytes(
                            StandardCharsets.UTF_8);

    private final SecureRandom secureRandom =
            new SecureRandom();

    private final byte[] encryptionKey;

    public MfaSecretProtector(
            @Value("${app.security.mfa.encryption-key:}")
            String encodedKey) {

        this.encryptionKey =
                parseKey(
                        encodedKey);
    }

    public boolean isConfigured() {
        return encryptionKey != null;
    }

    public String protect(
            String rawSecret) {

        requireConfigured();

        if (rawSecret == null
                || rawSecret.isBlank()) {

            throw new IllegalArgumentException(
                    "Secreto MFA requerido");
        }

        byte[] iv =
                new byte[IV_BYTES];

        secureRandom.nextBytes(
                iv);

        try {
            Cipher cipher =
                    Cipher.getInstance(
                            TRANSFORMATION);

            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    new SecretKeySpec(
                            encryptionKey,
                            "AES"),
                    new GCMParameterSpec(
                            TAG_BITS,
                            iv));

            cipher.updateAAD(
                    AAD);

            byte[] ciphertext =
                    cipher.doFinal(
                            rawSecret
                                    .getBytes(
                                            StandardCharsets.UTF_8));

            byte[] payload =
                    new byte[
                            iv.length
                                    + ciphertext.length];

            System.arraycopy(
                    iv,
                    0,
                    payload,
                    0,
                    iv.length);

            System.arraycopy(
                    ciphertext,
                    0,
                    payload,
                    iv.length,
                    ciphertext.length);

            return VERSION_PREFIX
                    + Base64
                    .getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(
                            payload);
        }
        catch (GeneralSecurityException ex) {

            throw new IllegalStateException(
                    "No se pudo proteger secreto MFA",
                    ex);
        }
    }

    public String unprotect(
            String protectedSecret) {

        requireConfigured();

        if (protectedSecret == null
                || protectedSecret.isBlank()
                || !protectedSecret.startsWith(
                        VERSION_PREFIX)) {

            throw invalidProtectedSecret();
        }

        try {
            byte[] payload =
                    Base64
                    .getUrlDecoder()
                    .decode(
                            protectedSecret.substring(
                                    VERSION_PREFIX.length()));

            if (payload.length <= IV_BYTES) {
                throw invalidProtectedSecret();
            }

            byte[] iv =
                    Arrays.copyOfRange(
                            payload,
                            0,
                            IV_BYTES);

            byte[] ciphertext =
                    Arrays.copyOfRange(
                            payload,
                            IV_BYTES,
                            payload.length);

            Cipher cipher =
                    Cipher.getInstance(
                            TRANSFORMATION);

            cipher.init(
                    Cipher.DECRYPT_MODE,
                    new SecretKeySpec(
                            encryptionKey,
                            "AES"),
                    new GCMParameterSpec(
                            TAG_BITS,
                            iv));

            cipher.updateAAD(
                    AAD);

            byte[] plaintext =
                    cipher.doFinal(
                            ciphertext);

            return new String(
                    plaintext,
                    StandardCharsets.UTF_8);
        }
        catch (IllegalArgumentException ex) {
            throw ex;
        }
        catch (GeneralSecurityException ex) {
            throw invalidProtectedSecret();
        }
    }

    private byte[] parseKey(
            String encodedKey) {

        if (encodedKey == null
                || encodedKey.isBlank()) {

            return null;
        }

        byte[] decoded;

        try {
            decoded =
                    Base64
                            .getDecoder()
                            .decode(
                                    encodedKey.trim());
        }
        catch (IllegalArgumentException ex) {

            throw new IllegalArgumentException(
                    "Clave MFA debe ser Base64 valida");
        }

        if (decoded.length != KEY_BYTES) {

            throw new IllegalArgumentException(
                    "Clave MFA debe contener 32 bytes");
        }

        return decoded;
    }

    private void requireConfigured() {

        if (!isConfigured()) {
            throw new IllegalStateException(
                    "Clave MFA no configurada");
        }
    }

    private IllegalArgumentException invalidProtectedSecret() {

        return new IllegalArgumentException(
                "Secreto MFA protegido invalido");
    }
}
