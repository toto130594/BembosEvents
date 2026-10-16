package com.bembosevents.dto.sede;

public record SedeResumenResponse(

        Integer idSede,
        String nombre,
        String direccion,
        String distrito,
        String telefono,
        Boolean activo
) {
}