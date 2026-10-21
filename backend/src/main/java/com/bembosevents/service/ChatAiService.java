package com.bembosevents.service;

import com.bembosevents.dto.chat.MensajeChatResponse;

import java.util.Optional;

public interface ChatAiService {

    Optional<MensajeChatResponse> responder(
            Integer idConversacion,
            String mensajeCliente
    );
}