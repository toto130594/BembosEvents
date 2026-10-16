package com.bembosevents.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    @Test
    void debeGenerarYValidarToken() {

        String secret = Base64
                .getEncoder()
                .encodeToString(
                        "01234567890123456789012345678901"
                                .getBytes(StandardCharsets.UTF_8));

        JwtService jwtService =
                new JwtService(
                        secret,
                        3600000);

        UserDetails user =
                User.withUsername(
                                "cliente@bembos.test")
                        .password("hash")
                        .roles("CLIENTE")
                        .build();

        String token =
                jwtService.generateToken(user);

        assertEquals(
                "cliente@bembos.test",
                jwtService.extractUsername(token));

        assertTrue(
                jwtService.isTokenValid(
                        token,
                        user));
    }
}