package com.bembosevents.service;

import com.bembosevents.ai.orchestration.AiConversationOrchestrator;
import com.bembosevents.ai.orchestration.AiConversationRequest;
import com.bembosevents.dto.chat.ConversacionDetalleResponse;
import com.bembosevents.entity.Conversacion;
import com.bembosevents.entity.MensajeChat;
import com.bembosevents.repository.ConversacionRepository;
import com.bembosevents.repository.MensajeChatRepository;
import com.bembosevents.service.impl.ChatAiServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatAiServiceImplTest {

    @Mock
    private AiConversationOrchestrator orchestrator;

    @Mock
    private ChatClienteService chatClienteService;

    @Mock
    private ConversacionRepository conversacionRepository;

    @Mock
    private MensajeChatRepository mensajeChatRepository;

    @Test
    void botOrquestaConIdentidadYPersisteRespuestaIa() {
        when(chatClienteService.obtenerPropia(
                "cliente@test.com",
                15))
                .thenReturn(
                        detalle(
                                15,
                                4,
                                3,
                                "BOT"));

        Conversacion conversacion =
                new Conversacion();

        conversacion.setIdConversacion(
                15);

        when(conversacionRepository.findById(
                15))
                .thenReturn(
                        Optional.of(
                                conversacion));

        when(orchestrator.responder(
                any(AiConversationRequest.class)))
                .thenReturn(
                        "Respuesta orquestada");

        when(mensajeChatRepository.save(
                any(MensajeChat.class)))
                .thenAnswer(invocation -> {
                    MensajeChat message =
                            invocation.getArgument(0);

                    message.setIdMensajeChat(
                            30);

                    return message;
                });

        ChatAiServiceImpl service =
                service();

        var response =
                service.responder(
                        "cliente@test.com",
                        15,
                        "Hola");

        assertTrue(
                response.isPresent());

        assertEquals(
                "IA",
                response.orElseThrow()
                        .tipoEmisor());

        assertEquals(
                "Respuesta orquestada",
                response.orElseThrow()
                        .contenido());

        ArgumentCaptor<AiConversationRequest> captor =
                ArgumentCaptor.forClass(
                        AiConversationRequest.class);

        verify(orchestrator)
                .responder(
                        captor.capture());

        assertEquals(
                "cliente@test.com",
                captor.getValue()
                        .correoCliente());

        assertEquals(
                3,
                captor.getValue()
                        .idSede());
    }

    @Test
    void conversacionFueraDeBotNoInvocaOrquestador() {
        when(chatClienteService.obtenerPropia(
                "cliente@test.com",
                22))
                .thenReturn(
                        detalle(
                                22,
                                4,
                                3,
                                "EN_ATENCION"));

        ChatAiServiceImpl service =
                service();

        var response =
                service.responder(
                        "cliente@test.com",
                        22,
                        "Hola");

        assertTrue(
                response.isEmpty());

        verify(orchestrator, never())
                .responder(
                        any());

        verify(conversacionRepository, never())
                .findById(
                        any());
    }

    private ChatAiServiceImpl service() {
        return new ChatAiServiceImpl(
                orchestrator,
                chatClienteService,
                conversacionRepository,
                mensajeChatRepository);
    }

    private ConversacionDetalleResponse detalle(
            Integer idConversacion,
            Integer idCliente,
            Integer idSede,
            String estado) {

        return new ConversacionDetalleResponse(
                idConversacion,
                idCliente,
                idSede,
                estado,
                null,
                null,
                null,
                List.of());
    }
}