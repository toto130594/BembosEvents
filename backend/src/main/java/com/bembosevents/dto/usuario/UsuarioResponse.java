package com.bembosevents.dto.usuario;

import java.util.List;

public record UsuarioResponse(

        Integer idUsuario,
        String nombres,
        String apellidos,
        String correo,
        String telefono,
        Boolean activo,
        List<String> roles,
        Integer idSede,
        String nombreSede
) {
}