package com.bembosevents.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RefreshRequest(

        @NotBlank(
                message = "El refresh token es obligatorio")
        @Size(
                max = 500,
                message = "El refresh token no puede superar 500 caracteres")
        String refreshToken) {
}