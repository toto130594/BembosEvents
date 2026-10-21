package com.bembosevents.service;

import com.bembosevents.dto.chat.ConversacionResumenResponse;
import com.bembosevents.dto.chat.EnviarMensajeChatRequest;
import com.bembosevents.dto.chat.MensajeChatResponse;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface ChatEscalationService {

    ConversacionResumenResponse solicitarEscalamiento(
            String correoCliente,
            Integer idConversacion
    );

    List<ConversacionResumenResponse> listarPendientes(
            Authentication authentication
    );

    ConversacionResumenResponse tomarConversacion(
            Integer idConversacion,
            Authentication authentication
    );

    MensajeChatResponse enviarMensajeEncargado(
            Integer idConversacion,
            Authentication authentication,
            EnviarMensajeChatRequest request
    );
}