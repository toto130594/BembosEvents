package com.bembosevents.dto.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthResponseTest {

    @Test
    void authenticatedIncluyeRefreshSinMfaChallenge() {

        AuthResponse response =
                AuthResponse.authenticated(
                        "access",
                        "Bearer",
                        3600000L,
                        "refresh",
                        2592000000L);

        assertEquals(
                "access",
                response.token());

        assertEquals(
                "refresh",
                response.refreshToken());

        assertFalse(
                response.mfaRequired());

        assertNull(
                response.mfaChallengeToken());
    }

    @Test
    void mfaRequiredNoIncluyeTokensDeSesion() {

        AuthResponse response =
                AuthResponse.mfaRequired(
                        "challenge",
                        300000L);

        assertTrue(
                response.mfaRequired());

        assertNull(
                response.token());

        assertNull(
                response.refreshToken());

        assertEquals(
                0L,
                response.expiresInMs());

        assertEquals(
                0L,
                response.refreshTokenExpiresInMs());

        assertEquals(
                "challenge",
                response.mfaChallengeToken());
    }

    @Test
    void constructorLegacySigueDisponible() {

        AuthResponse response =
                new AuthResponse(
                        "access",
                        "Bearer",
                        3600000L);

        assertEquals(
                "access",
                response.token());

        assertNull(
                response.refreshToken());

        assertFalse(
                response.mfaRequired());
    }
}