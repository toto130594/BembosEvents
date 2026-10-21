package com.bembosevents.service;

import com.bembosevents.dto.chat.ChatBackofficeNotification;
import com.bembosevents.dto.chat.ConversacionResumenResponse;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ChatNotificationPublisher {

    public static final String GLOBAL_TOPIC =
            "/topic/backoffice/chat/notificaciones";

    public static final String SEDE_TOPIC_PREFIX =
            "/topic/backoffice/chat/sedes/";

    private final SimpMessagingTemplate messagingTemplate;

    public ChatNotificationPublisher(
            SimpMessagingTemplate messagingTemplate) {

        this.messagingTemplate =
                messagingTemplate;
    }

    public void notificarEscalamiento(
            ConversacionResumenResponse conversacion) {

        if (conversacion == null
                || conversacion.idConversacion() == null) {

            throw new IllegalArgumentException(
                    "La conversacion es obligatoria");
        }

        if (conversacion.idSede() == null) {
            throw new IllegalArgumentException(
                    "La notificacion BackOffice requiere sede");
        }

        ChatBackofficeNotification notification =
                new ChatBackofficeNotification(
                        "CONVERSACION_ESCALADA",
                        conversacion.idConversacion(),
                        conversacion.idSede(),
                        conversacion.estado(),
                        "Nueva conversacion esperando atencion",
                        LocalDateTime.now()
                );

        messagingTemplate.convertAndSend(
                GLOBAL_TOPIC,
                notification
        );

        messagingTemplate.convertAndSend(
                SEDE_TOPIC_PREFIX
                        + conversacion.idSede()
                        + "/notificaciones",
                notification
        );
    }
}