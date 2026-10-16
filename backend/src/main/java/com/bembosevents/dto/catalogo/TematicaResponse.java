package com.bembosevents.dto.catalogo;

import java.math.BigDecimal;

public record TematicaResponse(
        Integer idTematica,
        String nombre,
        String descripcion,
        BigDecimal precioAdicional) {
}
