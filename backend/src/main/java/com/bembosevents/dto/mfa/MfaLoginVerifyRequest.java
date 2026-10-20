package com.bembosevents.dto.mfa;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record MfaLoginVerifyRequest(

        @NotBlank(message = "El challenge MFA es obligatorio")
        String challengeToken,

        @NotBlank(message = "El codigo MFA es obligatorio")
        @Pattern(
                regexp = "\\d{6}",
                message = "El codigo MFA debe tener 6 digitos")
        String code) {
}