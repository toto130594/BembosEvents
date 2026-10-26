package com.bembosevents.dto.catalogo;

public record SedePublicaResponse(
        Integer idSede,
        String nombre,
        String direccion,
        String distrito,
        String telefono,
        String tipoLocal,
        Boolean permiteEventos) {

    public SedePublicaResponse(
            Integer idSede,
            String nombre,
            String direccion,
            String distrito,
            String telefono) {

        this(
                idSede,
                nombre,
                direccion,
                distrito,
                telefono,
                "RESTAURANTE",
                Boolean.TRUE);
    }
}