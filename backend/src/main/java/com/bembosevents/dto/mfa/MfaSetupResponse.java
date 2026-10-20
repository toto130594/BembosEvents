package com.bembosevents.dto.mfa;

public record MfaSetupResponse(
        String secret,
        String otpauthUri,
        boolean mfaHabilitado) {
}