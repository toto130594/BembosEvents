package com.bembosevents.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordResetRequest(

        @NotBlank(message = "El token es obligatorio")
        @Size(
                max = 500,
                message = "El token no puede superar 500 caracteres")
        String token,

        @NotBlank(message = "La contrasena es obligatoria")
        @Size(
                min = 8,
                max = 100,
                message = "La contrasena debe tener entre 8 y 100 caracteres")
        String nuevaPassword) {
}
