package com.bembosevents.controller;

import com.bembosevents.dto.chat.ChatRealtimeEvent;
import com.bembosevents.dto.chat.EnviarMensajeChatRequest;
import com.bembosevents.dto.chat.MensajeChatResponse;
import com.bembosevents.security.chat.ChatWebSocketAuthorizationService;
import com.bembosevents.service.ChatAiService;
import com.bembosevents.service.ChatClienteService;
import jakarta.validation.Valid;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class ChatWebSocketController {

    private final ChatClienteService chatClienteService;
    private final ChatAiService chatAiService;
    private final ChatWebSocketAuthorizationService
            authorizationService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatWebSocketController(
            ChatClienteService chatClienteService,
            ChatAiService chatAiService,
            ChatWebSocketAuthorizationService authorizationService,
            SimpMessagingTemplate messagingTemplate) {

        this.chatClienteService =
                chatClienteService;

        this.chatAiService =
                chatAiService;

        this.authorizationService =
                authorizationService;

        this.messagingTemplate =
                messagingTemplate;
    }

    @MessageMapping(
            "/chat/conversaciones/{idConversacion}/mensajes"
    )
    public void enviarMensaje(
            Principal principal,
            @DestinationVariable Integer idConversacion,
            @Valid EnviarMensajeChatRequest request) {

        Authentication authentication =
                exigirAuthentication(principal);

        authorizationService.validarEnvioCliente(
                authentication,
                idConversacion);

        MensajeChatResponse mensajeCliente =
                chatClienteService.enviarMensaje(
                        authentication.getName(),
                        idConversacion,
                        request);

        publicar(
                idConversacion,
                mensajeCliente);

        chatAiService
                .responder(
                        idConversacion,
                        request.contenido())
                .ifPresent(mensajeIa ->
                        publicar(
                                idConversacion,
                                mensajeIa)
                );
    }

    private void publicar(
            Integer idConversacion,
            MensajeChatResponse mensaje) {

        messagingTemplate.convertAndSend(
                "/topic/chat/conversaciones/"
                        + idConversacion,
                ChatRealtimeEvent.mensaje(
                        idConversacion,
                        mensaje)
        );
    }

    private Authentication exigirAuthentication(
            Principal principal) {

        if (!(principal instanceof Authentication authentication)
                || !authentication.isAuthenticated()) {

            throw new AccessDeniedException(
                    "Autenticacion WebSocket requerida");
        }

        return authentication;
    }
}