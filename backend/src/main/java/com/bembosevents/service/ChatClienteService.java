package com.bembosevents.service;

import com.bembosevents.dto.chat.ConversacionDetalleResponse;
import com.bembosevents.dto.chat.ConversacionResumenResponse;
import com.bembosevents.dto.chat.CrearConversacionRequest;
import com.bembosevents.dto.chat.EnviarMensajeChatRequest;
import com.bembosevents.dto.chat.MensajeChatResponse;

import java.util.List;

public interface ChatClienteService {

    ConversacionDetalleResponse crear(
            String correoCliente,
            CrearConversacionRequest request
    );

    List<ConversacionResumenResponse> listarPropias(
            String correoCliente
    );

    ConversacionDetalleResponse obtenerPropia(
            String correoCliente,
            Integer idConversacion
    );

    MensajeChatResponse enviarMensaje(
            String correoCliente,
            Integer idConversacion,
            EnviarMensajeChatRequest request
    );
}