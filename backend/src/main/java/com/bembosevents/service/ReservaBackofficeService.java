package com.bembosevents.service;

import com.bembosevents.dto.reserva.CambiarEstadoReservaRequest;
import com.bembosevents.dto.reserva.ReservaDetalleResponse;
import com.bembosevents.dto.reserva.ReservaResumenResponse;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface ReservaBackofficeService {

    List<ReservaResumenResponse> listarAccesibles(
            Authentication authentication);

    ReservaDetalleResponse obtenerAccesible(
            Integer idReserva,
            Authentication authentication);

    ReservaDetalleResponse cambiarEstado(
            Integer idReserva,
            CambiarEstadoReservaRequest request,
            Authentication authentication);
}