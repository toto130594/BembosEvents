package com.bembosevents.controller;

import com.bembosevents.dto.chat.ChatRealtimeEvent;
import com.bembosevents.dto.chat.EnviarMensajeChatRequest;
import com.bembosevents.dto.chat.MensajeChatResponse;
import com.bembosevents.security.chat.ChatWebSocketAuthorizationService;
import com.bembosevents.service.ChatAiService;
import com.bembosevents.service.ChatClienteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatWebSocketControllerTest {

    @Mock
    private ChatClienteService chatClienteService;

    @Mock
    private ChatAiService chatAiService;

    @Mock
    private ChatWebSocketAuthorizationService
            authorizationService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Test
    void mensajeClienteYRespuestaIaSePublicanAlTopicConIdentidad() {
        UsernamePasswordAuthenticationToken authentication =
                UsernamePasswordAuthenticationToken.authenticated(
                        "cliente@test.com",
                        null,
                        List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_CLIENTE")));

        EnviarMensajeChatRequest request =
                new EnviarMensajeChatRequest(
                        "Hola Bembos");

        MensajeChatResponse cliente =
                new MensajeChatResponse(
                        44,
                        "CLIENTE",
                        9,
                        "Hola Bembos",
                        LocalDateTime.of(
                                2026,
                                10,
                                21,
                                12,
                                0));

        MensajeChatResponse ia =
                new MensajeChatResponse(
                        45,
                        "IA",
                        null,
                        "Hola. Soy el asistente virtual.",
                        LocalDateTime.of(
                                2026,
                                10,
                                21,
                                12,
                                0,
                                1));

        when(chatClienteService.enviarMensaje(
                "cliente@test.com",
                12,
                request))
                .thenReturn(
                        cliente);

        when(chatAiService.responder(
                "cliente@test.com",
                12,
                "Hola Bembos"))
                .thenReturn(
                        Optional.of(
                                ia));

        ChatWebSocketController controller =
                new ChatWebSocketController(
                        chatClienteService,
                        chatAiService,
                        authorizationService,
                        messagingTemplate);

        controller.enviarMensaje(
                authentication,
                12,
                request);

        verify(authorizationService)
                .validarEnvioCliente(
                        authentication,
                        12);

        verify(chatAiService)
                .responder(
                        "cliente@test.com",
                        12,
                        "Hola Bembos");

        verify(messagingTemplate)
                .convertAndSend(
                        eq("/topic/chat/conversaciones/12"),
                        eq((Object)
                                ChatRealtimeEvent.mensaje(
                                        12,
                                        cliente)));

        verify(messagingTemplate)
                .convertAndSend(
                        eq("/topic/chat/conversaciones/12"),
                        eq((Object)
                                ChatRealtimeEvent.mensaje(
                                        12,
                                        ia)));
    }
}