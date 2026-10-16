package com.bembosevents.dto.auth;

public record AuthResponse(
        String token,
        String tipo,
        long expiresInMs) {
}