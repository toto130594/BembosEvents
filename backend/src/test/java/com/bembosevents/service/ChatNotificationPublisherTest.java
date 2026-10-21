package com.bembosevents.service;

import com.bembosevents.dto.chat.ChatBackofficeNotification;
import com.bembosevents.dto.chat.ConversacionResumenResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ChatNotificationPublisherTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Test
    void escalamientoPublicaGlobalYSede() {

        ChatNotificationPublisher publisher =
                new ChatNotificationPublisher(
                        messagingTemplate);

        ConversacionResumenResponse conversation =
                new ConversacionResumenResponse(
                        20,
                        7,
                        "ESPERANDO_ENCARGADO",
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
                        null
                );

        publisher.notificarEscalamiento(
                conversation);

        ArgumentCaptor<ChatBackofficeNotification> captor =
                ArgumentCaptor.forClass(
                        ChatBackofficeNotification.class);

        verify(
                messagingTemplate,
                times(1)
        ).convertAndSend(
                eq(ChatNotificationPublisher.GLOBAL_TOPIC),
                captor.capture()
        );

        ChatBackofficeNotification notification =
                captor.getValue();

        assertEquals(
                "CONVERSACION_ESCALADA",
                notification.tipo()
        );

        assertEquals(
                20,
                notification.idConversacion()
        );

        assertEquals(
                7,
                notification.idSede()
        );

        verify(
                messagingTemplate,
                times(1)
        ).convertAndSend(
                eq(
                        "/topic/backoffice/chat/sedes/7/notificaciones"),
                eq((Object) notification)
        );
    }
}