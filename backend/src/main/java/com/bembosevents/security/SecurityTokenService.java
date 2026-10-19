package com.bembosevents.security;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class SecurityTokenService {

    private static final int TOKEN_BYTES = 32;

    private final SecureRandom secureRandom;

    public SecurityTokenService() {
        this.secureRandom = new SecureRandom();
    }

    public String generateOpaqueToken() {
        byte[] randomBytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(randomBytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);
    }

    public String hashToken(String rawToken) {
        validateRawToken(rawToken);

        return HexFormat.of().formatHex(
                sha256(rawToken)
        );
    }

    public boolean matches(
            String rawToken,
            String expectedHash) {

        if (rawToken == null
                || rawToken.isBlank()
                || expectedHash == null
                || expectedHash.isBlank()) {
            return false;
        }

        try {
            byte[] actual =
                    sha256(rawToken);

            byte[] expected =
                    HexFormat.of().parseHex(
                            expectedHash
                    );

            return MessageDigest.isEqual(
                    actual,
                    expected
            );
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private byte[] sha256(String rawToken) {
        try {
            MessageDigest digest =
                    MessageDigest.getInstance(
                            "SHA-256"
                    );

            return digest.digest(
                    rawToken.getBytes(
                            StandardCharsets.UTF_8
                    )
            );
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(
                    "SHA-256 no disponible",
                    ex
            );
        }
    }

    private void validateRawToken(
            String rawToken) {

        if (rawToken == null
                || rawToken.isBlank()) {
            throw new IllegalArgumentException(
                    "El token no puede estar vacio"
            );
        }
    }
}
