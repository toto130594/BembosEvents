package com.bembosevents.dto.auth;

public record AuthResponse(
        String token,
        String tipo,
        long expiresInMs,
        String refreshToken,
        long refreshTokenExpiresInMs,
        boolean mfaRequired,
        String mfaChallengeToken,
        long mfaChallengeExpiresInMs) {

    public AuthResponse(
            String token,
            String tipo,
            long expiresInMs) {

        this(
                token,
                tipo,
                expiresInMs,
                null,
                0L,
                false,
                null,
                0L);
    }

    public static AuthResponse authenticated(
            String token,
            String tipo,
            long expiresInMs,
            String refreshToken,
            long refreshTokenExpiresInMs) {

        if (token == null
                || token.isBlank()) {

            throw new IllegalArgumentException(
                    "token es requerido");
        }

        if (tipo == null
                || tipo.isBlank()) {

            throw new IllegalArgumentException(
                    "tipo es requerido");
        }

        if (expiresInMs < 1) {
            throw new IllegalArgumentException(
                    "expiresInMs debe ser mayor que cero");
        }

        if (refreshToken == null
                || refreshToken.isBlank()) {

            throw new IllegalArgumentException(
                    "refreshToken es requerido");
        }

        if (refreshTokenExpiresInMs < 1) {
            throw new IllegalArgumentException(
                    "refreshTokenExpiresInMs debe ser mayor que cero");
        }

        return new AuthResponse(
                token,
                tipo,
                expiresInMs,
                refreshToken,
                refreshTokenExpiresInMs,
                false,
                null,
                0L);
    }

    public static AuthResponse mfaRequired(
            String challengeToken,
            long challengeExpiresInMs) {

        if (challengeToken == null
                || challengeToken.isBlank()) {

            throw new IllegalArgumentException(
                    "challengeToken es requerido");
        }

        if (challengeExpiresInMs < 1) {
            throw new IllegalArgumentException(
                    "challengeExpiresInMs debe ser mayor que cero");
        }

        return new AuthResponse(
                null,
                null,
                0L,
                null,
                0L,
                true,
                challengeToken,
                challengeExpiresInMs);
    }
}