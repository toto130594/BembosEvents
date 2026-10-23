package com.bembosevents.ai.draft;

public record AiDraftServiceSelection(
        Integer idServicioAdicional,
        Integer cantidad) {

    public AiDraftServiceSelection {
        if (idServicioAdicional == null
                || idServicioAdicional <= 0) {
            throw new IllegalArgumentException(
                    "El servicio adicional debe ser valido");
        }

        if (cantidad == null
                || cantidad <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad del servicio debe ser mayor que cero");
        }
    }
}