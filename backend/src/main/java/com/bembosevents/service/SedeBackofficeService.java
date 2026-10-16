package com.bembosevents.service;

import com.bembosevents.dto.sede.SedeResumenResponse;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface SedeBackofficeService {

    List<SedeResumenResponse> listarAccesibles(
            Authentication authentication
    );

    SedeResumenResponse obtenerAccesible(
            Integer idSede,
            Authentication authentication
    );
}