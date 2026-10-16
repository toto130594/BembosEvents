package com.bembosevents.dto.catalogo;

public record SalonPublicoResponse(
        Integer idSalon,
        Integer idSede,
        String nombre,
        String descripcion,
        Integer capacidad) {
}
