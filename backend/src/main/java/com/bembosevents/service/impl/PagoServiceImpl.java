package com.bembosevents.service.impl;

import com.bembosevents.domain.EstadoPago;
import com.bembosevents.domain.ReglasTransicionPago;
import com.bembosevents.service.PagoService;
import org.springframework.stereotype.Service;

@Service
public class PagoServiceImpl implements PagoService {

    @Override
    public void validarTransicionEstado(
            EstadoPago estadoActual,
            EstadoPago nuevoEstado) {

        if (!ReglasTransicionPago.esValida(
                estadoActual,
                nuevoEstado)) {

            throw new IllegalArgumentException(
                    "Transicion de estado de pago no permitida");
        }
    }
}