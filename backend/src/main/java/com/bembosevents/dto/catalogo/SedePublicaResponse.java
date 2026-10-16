package com.bembosevents.dto.catalogo;

public record SedePublicaResponse(
        Integer idSede,
        String nombre,
        String direccion,
        String distrito,
        String telefono) {
}
