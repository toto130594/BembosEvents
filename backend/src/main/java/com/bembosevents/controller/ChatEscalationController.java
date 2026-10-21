package com.bembosevents.controller;

import com.bembosevents.dto.chat.ChatEstadoRealtimeEvent;
import com.bembosevents.dto.chat.ChatRealtimeEvent;
import com.bembosevents.dto.chat.ConversacionResumenResponse;
import com.bembosevents.dto.chat.EnviarMensajeChatRequest;
import com.bembosevents.dto.chat.MensajeChatResponse;
import com.bembosevents.service.ChatEscalationService;
import com.bembosevents.service.ChatNotificationPublisher;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ChatEscalationController {

    private final ChatEscalationService chatEscalationService;
    private final ChatNotificationPublisher notificationPublisher;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatEscalationController(
            ChatEscalationService chatEscalationService,
            ChatNotificationPublisher notificationPublisher,
            SimpMessagingTemplate messagingTemplate) {

        this.chatEscalationService =
                chatEscalationService;

        this.notificationPublisher =
                notificationPublisher;

        this.messagingTemplate =
                messagingTemplate;
    }

    @PostMapping(
            "/api/chat/conversaciones/{idConversacion}/escalar"
    )
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<ConversacionResumenResponse>
    solicitarEscalamiento(
            Authentication authentication,
            @PathVariable Integer idConversacion) {

        ConversacionResumenResponse response =
                chatEscalationService
                        .solicitarEscalamiento(
                                authentication.getName(),
                                idConversacion);

        publicarEstado(
                response);

        notificationPublisher
                .notificarEscalamiento(
                        response);

        return ResponseEntity.ok(
                response);
    }

    @GetMapping(
            "/api/backoffice/chat/conversaciones/pendientes"
    )
    @PreAuthorize(
            "hasAnyRole('ENCARGADO_LOCAL','ADMINISTRADOR')"
    )
    public ResponseEntity<List<ConversacionResumenResponse>>
    listarPendientes(
            Authentication authentication) {

        return ResponseEntity.ok(
                chatEscalationService
                        .listarPendientes(
                                authentication)
        );
    }

    @PostMapping(
            "/api/backoffice/chat/conversaciones/{idConversacion}/tomar"
    )
    @PreAuthorize("hasRole('ENCARGADO_LOCAL')")
    public ResponseEntity<ConversacionResumenResponse>
    tomarConversacion(
            Authentication authentication,
            @PathVariable Integer idConversacion) {

        ConversacionResumenResponse response =
                chatEscalationService
                        .tomarConversacion(
                                idConversacion,
                                authentication);

        publicarEstado(
                response);

        return ResponseEntity.ok(
                response);
    }

    @PostMapping(
            "/api/backoffice/chat/conversaciones/{idConversacion}/mensajes"
    )
    @PreAuthorize("hasRole('ENCARGADO_LOCAL')")
    public ResponseEntity<MensajeChatResponse>
    enviarMensajeEncargado(
            Authentication authentication,
            @PathVariable Integer idConversacion,
            @Valid @RequestBody EnviarMensajeChatRequest request) {

        MensajeChatResponse response =
                chatEscalationService
                        .enviarMensajeEncargado(
                                idConversacion,
                                authentication,
                                request);

        messagingTemplate.convertAndSend(
                "/topic/chat/conversaciones/"
                        + idConversacion,
                ChatRealtimeEvent.mensaje(
                        idConversacion,
                        response)
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    private void publicarEstado(
            ConversacionResumenResponse response) {

        messagingTemplate.convertAndSend(
                "/topic/chat/conversaciones/"
                        + response.idConversacion(),
                new ChatEstadoRealtimeEvent(
                        response.idConversacion(),
                        response.estado())
        );
    }
}