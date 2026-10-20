package com.bembosevents.dto.mfa;

import jakarta.validation.constraints.NotBlank;

public record MfaRecoveryLoginRequest(

        @NotBlank(message = "El challenge MFA es obligatorio")
        String challengeToken,

        @NotBlank(message = "El recovery code es obligatorio")
        String recoveryCode) {
}