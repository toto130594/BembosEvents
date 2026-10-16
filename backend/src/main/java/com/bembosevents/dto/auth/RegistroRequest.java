package com.bembosevents.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistroRequest(

        @NotBlank(message = "Los nombres son obligatorios")
        @Size(max = 100, message = "Los nombres no pueden superar 100 caracteres")
        String nombres,

        @NotBlank(message = "Los apellidos son obligatorios")
        @Size(max = 100, message = "Los apellidos no pueden superar 100 caracteres")
        String apellidos,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato valido")
        @Size(max = 254, message = "El correo no puede superar 254 caracteres")
        String correo,

        @Size(max = 20, message = "El telefono no puede superar 20 caracteres")
        String telefono,

        @NotBlank(message = "La contrasena es obligatoria")
        @Size(
                min = 8,
                max = 72,
                message = "La contrasena debe tener entre 8 y 72 caracteres")
        String password) {
}