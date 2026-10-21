package com.bembosevents.controller;

import com.bembosevents.dto.chat.ConversacionDetalleResponse;
import com.bembosevents.dto.chat.MensajeChatResponse;
import com.bembosevents.service.ChatBackofficeService;
import com.bembosevents.service.ChatClienteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatHistoryControllerTest {

    @Mock
    private ChatClienteService chatClienteService;

    @Mock
    private ChatBackofficeService chatBackofficeService;

    @Test
    void clienteObtieneHistorialUsandoOwnershipExistente() {

        UsernamePasswordAuthenticationToken authentication =
                UsernamePasswordAuthenticationToken.authenticated(
                        "cliente@test.com",
                        null,
                        List.of()
                );

        MensajeChatResponse mensaje =
                mensaje();

        when(
                chatClienteService
                        .obtenerPropia(
                                "cliente@test.com",
                                10)
        ).thenReturn(
                detalle(
                        mensaje));

        ChatHistoryController controller =
                new ChatHistoryController(
                        chatClienteService,
                        chatBackofficeService
                );

        var response =
                controller.historialCliente(
                        authentication,
                        10);

        assertEquals(
                1,
                response.getBody().size()
        );

        verify(
                chatClienteService
        ).obtenerPropia(
                "cliente@test.com",
                10);
    }

    @Test
    void backofficeObtieneHistorialUsandoScopeExistente() {

        UsernamePasswordAuthenticationToken authentication =
                UsernamePasswordAuthenticationToken.authenticated(
                        "encargado@test.com",
                        null,
                        List.of()
                );

        MensajeChatResponse mensaje =
                mensaje();

        when(
                chatBackofficeService
                        .obtenerAccesible(
                                10,
                                authentication)
        ).thenReturn(
                detalle(
                        mensaje));

        ChatHistoryController controller =
                new ChatHistoryController(
                        chatClienteService,
                        chatBackofficeService
                );

        var response =
                controller.historialBackoffice(
                        authentication,
                        10);

        assertEquals(
                1,
                response.getBody().size()
        );

        verify(
                chatBackofficeService
        ).obtenerAccesible(
                10,
                authentication);
    }

    private ConversacionDetalleResponse detalle(
            MensajeChatResponse mensaje) {

        return new ConversacionDetalleResponse(
                10,
                3,
                7,
                "EN_ATENCION",
                LocalDateTime.of(
                        2026,
                        10,
                        21,
                        10,
                        0),
                LocalDateTime.of(
                        2026,
                        10,
                        21,
                        10,
                        5),
                null,
                List.of(
                        mensaje)
        );
    }

    private MensajeChatResponse mensaje() {

        return new MensajeChatResponse(
                1,
                "CLIENTE",
                2,
                "Hola",
                LocalDateTime.of(
                        2026,
                        10,
                        21,
                        10,
                        1)
        );
    }
}