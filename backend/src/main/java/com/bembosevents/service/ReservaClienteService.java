package com.bembosevents.service;

import com.bembosevents.dto.reserva.CrearReservaRequest;
import com.bembosevents.dto.reserva.ReservaCreadaResponse;
import com.bembosevents.dto.reserva.ReservaDetalleResponse;
import com.bembosevents.dto.reserva.ReservaResumenResponse;

import java.util.List;

public interface ReservaClienteService {

    ReservaCreadaResponse crear(
            String correoCliente,
            CrearReservaRequest request);

    List<ReservaResumenResponse> misReservas(
            String correoCliente);

    ReservaDetalleResponse consultarPropia(
            String correoCliente,
            Integer idReserva);

    ReservaDetalleResponse cancelarPropia(
            String correoCliente,
            Integer idReserva);
}
