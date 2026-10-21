package com.bembosevents.service;

import com.bembosevents.dto.chat.ConversacionDetalleResponse;
import com.bembosevents.dto.chat.ConversacionResumenResponse;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface ChatBackofficeService {

    List<ConversacionResumenResponse> listarAccesibles(
            Authentication authentication
    );

    ConversacionDetalleResponse obtenerAccesible(
            Integer idConversacion,
            Authentication authentication
    );
}