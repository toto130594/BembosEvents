package com.bembosevents.service;

import com.bembosevents.ai.ChatAiProvider;
import com.bembosevents.entity.Cliente;
import com.bembosevents.entity.Conversacion;
import com.bembosevents.entity.EstadoConversacion;
import com.bembosevents.entity.MensajeChat;
import com.bembosevents.repository.ConversacionRepository;
import com.bembosevents.repository.MensajeChatRepository;
import com.bembosevents.service.impl.ChatAiServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    private ChatAiProvider chatAiProvider;

    @Mock
    private ConversacionRepository conversacionRepository;

    @Mock
    private MensajeChatRepository mensajeChatRepository;

    @Test
    void botGeneraYPersisteRespuestaIa() {

        Cliente cliente =
                new Cliente();

        cliente.setIdCliente(4);

        Conversacion conversacion =
                new Conversacion();

        conversacion.setIdConversacion(15);
        conversacion.setCliente(cliente);
        conversacion.setEstado(
                EstadoConversacion.BOT);

        when(
                conversacionRepository.findById(15)
        ).thenReturn(
                Optional.of(conversacion));

        when(
                chatAiProvider.responder(any())
        ).thenReturn(
                "Respuesta basica IA");

        when(
                mensajeChatRepository.save(
                        any(MensajeChat.class))
        ).thenAnswer(invocation -> {
            MensajeChat mensaje =
                    invocation.getArgument(0);

            mensaje.setIdMensajeChat(30);

            return mensaje;
        });

        ChatAiServiceImpl service =
                new ChatAiServiceImpl(
                        chatAiProvider,
                        conversacionRepository,
                        mensajeChatRepository
                );

        var response =
                service.responder(
                        15,
                        "Hola");

        assertTrue(response.isPresent());
        assertEquals(
                "IA",
                response.orElseThrow().tipoEmisor()
        );
        assertEquals(
                "Respuesta basica IA",
                response.orElseThrow().contenido()
        );
    }

    @Test
    void conversacionFueraDeBotNoInvocaProvider() {

        Conversacion conversacion =
                new Conversacion();

        conversacion.setIdConversacion(22);
        conversacion.setEstado(
                EstadoConversacion.EN_ATENCION);

        when(
                conversacionRepository.findById(22)
        ).thenReturn(
                Optional.of(conversacion));

        ChatAiServiceImpl service =
                new ChatAiServiceImpl(
                        chatAiProvider,
                        conversacionRepository,
                        mensajeChatRepository
                );

        var response =
                service.responder(
                        22,
                        "Hola");

        assertTrue(response.isEmpty());

        verify(
                chatAiProvider,
                never()
        ).responder(any());
    }
}