package com.bembosevents.dto.auth;

import java.time.LocalDateTime;

public record SesionResponse(
        Long idSesionUsuario,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaExpiracion,
        LocalDateTime fechaUltimoUso,
        String userAgent,
        String ipOrigen) {
}