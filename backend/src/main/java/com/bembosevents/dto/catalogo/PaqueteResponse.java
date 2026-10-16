package com.bembosevents.dto.catalogo;

import java.math.BigDecimal;

public record PaqueteResponse(
        Integer idPaquete,
        String nombre,
        String descripcion,
        BigDecimal precioBase,
        Integer cantidadPersonasBase) {
}
