package com.bembosevents.dto.usuario;

import jakarta.validation.constraints.NotBlank;

public record ActualizarRolUsuarioRequest(

        @NotBlank
        String rol,

        Integer idSede
) {
}