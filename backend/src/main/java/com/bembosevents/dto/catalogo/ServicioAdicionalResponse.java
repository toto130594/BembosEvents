package com.bembosevents.dto.catalogo;

import java.math.BigDecimal;

public record ServicioAdicionalResponse(
        Integer idServicioAdicional,
        String nombre,
        String descripcion,
        BigDecimal precio) {
}
