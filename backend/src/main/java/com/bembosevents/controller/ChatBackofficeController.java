package com.bembosevents.controller;

import com.bembosevents.dto.chat.ConversacionDetalleResponse;
import com.bembosevents.dto.chat.ConversacionResumenResponse;
import com.bembosevents.service.ChatBackofficeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/backoffice/chat/conversaciones")
@PreAuthorize(
        "hasAnyRole('ENCARGADO_LOCAL','ADMINISTRADOR')"
)
public class ChatBackofficeController {

    private final ChatBackofficeService chatBackofficeService;

    public ChatBackofficeController(
            ChatBackofficeService chatBackofficeService) {

        this.chatBackofficeService = chatBackofficeService;
    }

    @GetMapping
    public ResponseEntity<List<ConversacionResumenResponse>>
    listarAccesibles(
            Authentication authentication) {

        return ResponseEntity.ok(
                chatBackofficeService.listarAccesibles(
                        authentication)
        );
    }

    @GetMapping("/{idConversacion}")
    public ResponseEntity<ConversacionDetalleResponse> obtenerAccesible(
            Authentication authentication,
            @PathVariable Integer idConversacion) {

        return ResponseEntity.ok(
                chatBackofficeService.obtenerAccesible(
                        idConversacion,
                        authentication)
        );
    }
}