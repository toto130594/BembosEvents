package com.bembosevents.service;

import com.bembosevents.domain.EstadoPago;
import com.bembosevents.dto.pago.CrearPagoRequest;
import com.bembosevents.dto.pago.PagoResponse;

import java.util.List;

public interface PagoService {

    PagoResponse iniciarPago(
            String correoCliente,
            Integer idReserva,
            CrearPagoRequest request);

    List<PagoResponse> listarPagosPropios(
            String correoCliente,
            Integer idReserva);

    PagoResponse obtenerPagoPropio(
            String correoCliente,
            Integer idPago);

    PagoResponse vincularReferenciaExterna(
            Integer idPago,
            String referenciaExterna);

    com.bembosevents.dto.pago.PagoResponse actualizarEstadoDesdeProveedor(
            String referenciaExterna,
            EstadoPago nuevoEstado);
    void validarTransicionEstado(
            EstadoPago estadoActual,
            EstadoPago nuevoEstado);
}