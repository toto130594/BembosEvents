package com.bembosevents.controller;

import com.bembosevents.dto.chat.ChatTurnResponse;
import com.bembosevents.dto.chat.ConversacionDetalleResponse;
import com.bembosevents.dto.chat.ConversacionResumenResponse;
import com.bembosevents.dto.chat.CrearConversacionRequest;
import com.bembosevents.dto.chat.EnviarMensajeChatRequest;
import com.bembosevents.dto.chat.MensajeChatResponse;
import com.bembosevents.service.ChatAiService;
import com.bembosevents.service.ChatClienteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/chat/conversaciones")
@PreAuthorize("hasRole('CLIENTE')")
public class ChatClienteController {

    private final ChatClienteService chatClienteService;
    private final ChatAiService chatAiService;

    public ChatClienteController(
            ChatClienteService chatClienteService,
            ChatAiService chatAiService) {

        this.chatClienteService =
                chatClienteService;

        this.chatAiService =
                chatAiService;
    }

    @PostMapping
    public ResponseEntity<ConversacionDetalleResponse> crear(
            Authentication authentication,
            @Valid @RequestBody CrearConversacionRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        chatClienteService.crear(
                                authentication.getName(),
                                request)
                );
    }

    @GetMapping
    public ResponseEntity<List<ConversacionResumenResponse>>
    listarPropias(
            Authentication authentication) {

        return ResponseEntity.ok(
                chatClienteService.listarPropias(
                        authentication.getName())
        );
    }

    @GetMapping("/{idConversacion}")
    public ResponseEntity<ConversacionDetalleResponse> obtenerPropia(
            Authentication authentication,
            @PathVariable Integer idConversacion) {

        return ResponseEntity.ok(
                chatClienteService.obtenerPropia(
                        authentication.getName(),
                        idConversacion)
        );
    }

    @PostMapping("/{idConversacion}/mensajes")
    public ResponseEntity<ChatTurnResponse> enviarMensaje(
            Authentication authentication,
            @PathVariable Integer idConversacion,
            @Valid @RequestBody EnviarMensajeChatRequest request) {

        MensajeChatResponse cliente =
                chatClienteService.enviarMensaje(
                        authentication.getName(),
                        idConversacion,
                        request);

        MensajeChatResponse ia =
                chatAiService
                        .responder(
                                idConversacion,
                                request.contenido())
                        .orElse(null);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        new ChatTurnResponse(
                                cliente,
                                ia)
                );
    }
}