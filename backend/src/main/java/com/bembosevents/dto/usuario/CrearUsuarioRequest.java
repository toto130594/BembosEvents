package com.bembosevents.dto.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CrearUsuarioRequest(

        @NotBlank
        @Size(max = 100)
        String nombres,

        @NotBlank
        @Size(max = 100)
        String apellidos,

        @NotBlank
        @Email
        @Size(max = 254)
        String correo,

        @Size(max = 20)
        String telefono,

        @NotBlank
        @Size(min = 8, max = 72)
        String password,

        @NotBlank
        String rol,

        Integer idSede
) {
}