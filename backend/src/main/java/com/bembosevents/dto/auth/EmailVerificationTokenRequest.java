package com.bembosevents.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EmailVerificationTokenRequest(

        @NotBlank(message = "El token es obligatorio")
        @Size(
                max = 500,
                message = "El token no puede superar 500 caracteres")
        String token) {
}
