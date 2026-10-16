package com.bembosevents.service;

import com.bembosevents.dto.reserva.CrearReservaRequest;
import com.bembosevents.dto.reserva.ReservaCreadaResponse;

public interface ReservaClienteService {

    ReservaCreadaResponse crear(
            String correoCliente,
            CrearReservaRequest request);
}
