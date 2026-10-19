package com.bembosevents.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordRecoveryRequest(

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato valido")
        @Size(
                max = 254,
                message = "El correo no puede superar 254 caracteres")
        String correo) {
}
