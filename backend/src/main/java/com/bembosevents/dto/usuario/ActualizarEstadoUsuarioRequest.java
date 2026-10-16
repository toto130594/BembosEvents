package com.bembosevents.dto.usuario;

import jakarta.validation.constraints.NotNull;

public record ActualizarEstadoUsuarioRequest(

        @NotNull
        Boolean activo
) {
}