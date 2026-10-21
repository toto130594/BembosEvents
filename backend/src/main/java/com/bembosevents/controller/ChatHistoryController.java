package com.bembosevents.controller;

import com.bembosevents.dto.chat.MensajeChatResponse;
import com.bembosevents.service.ChatBackofficeService;
import com.bembosevents.service.ChatClienteService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ChatHistoryController {

    private final ChatClienteService chatClienteService;
    private final ChatBackofficeService chatBackofficeService;

    public ChatHistoryController(
            ChatClienteService chatClienteService,
            ChatBackofficeService chatBackofficeService) {

        this.chatClienteService =
                chatClienteService;

        this.chatBackofficeService =
                chatBackofficeService;
    }

    @GetMapping(
            "/api/chat/conversaciones/{idConversacion}/historial"
    )
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<List<MensajeChatResponse>>
    historialCliente(
            Authentication authentication,
            @PathVariable Integer idConversacion) {

        return ResponseEntity.ok(
                chatClienteService
                        .obtenerPropia(
                                authentication.getName(),
                                idConversacion)
                        .mensajes()
        );
    }

    @GetMapping(
            "/api/backoffice/chat/conversaciones/{idConversacion}/historial"
    )
    @PreAuthorize(
            "hasAnyRole('ENCARGADO_LOCAL','ADMINISTRADOR')"
    )
    public ResponseEntity<List<MensajeChatResponse>>
    historialBackoffice(
            Authentication authentication,
            @PathVariable Integer idConversacion) {

        return ResponseEntity.ok(
                chatBackofficeService
                        .obtenerAccesible(
                                idConversacion,
                                authentication)
                        .mensajes()
        );
    }
}