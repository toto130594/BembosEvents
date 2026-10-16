package com.bembosevents.service;

import com.bembosevents.domain.EstadoPago;

public interface PagoService {

    void validarTransicionEstado(
            EstadoPago estadoActual,
            EstadoPago nuevoEstado);
}