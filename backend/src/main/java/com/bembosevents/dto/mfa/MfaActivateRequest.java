package com.bembosevents.dto.mfa;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record MfaActivateRequest(

        @NotBlank(
                message = "El codigo TOTP es obligatorio")
        @Pattern(
                regexp = "\\d{6}",
                message = "El codigo TOTP debe contener 6 digitos")
        String code) {
}