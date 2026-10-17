package com.bembosevents.service;

import com.bembosevents.dto.pago.PagoResponse;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface PagoBackofficeService {

    List<PagoResponse> listarAccesibles(
            Authentication authentication);

    PagoResponse obtenerAccesible(
            Integer idPago,
            Authentication authentication);
}