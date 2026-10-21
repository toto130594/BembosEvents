package com.bembosevents.security.chat;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ChatStompChannelInterceptor
        implements ChannelInterceptor {

    private static final Pattern CONVERSATION_TOPIC =
            Pattern.compile(
                    "^/topic/chat/conversaciones/(\\d+)$"
            );

    private static final Pattern CLIENT_SEND_DESTINATION =
            Pattern.compile(
                    "^/app/chat/conversaciones/(\\d+)/mensajes$"
            );

    private static final Pattern SEDE_NOTIFICATION_TOPIC =
            Pattern.compile(
                    "^/topic/backoffice/chat/sedes/(\\d+)/notificaciones$"
            );

    private static final String GLOBAL_NOTIFICATION_TOPIC =
            "/topic/backoffice/chat/notificaciones";

    private final ChatWebSocketAuthorizationService
            authorizationService;

    public ChatStompChannelInterceptor(
            ChatWebSocketAuthorizationService authorizationService) {

        this.authorizationService =
                authorizationService;
    }

    @Override
    public Message<?> preSend(
            Message<?> message,
            MessageChannel channel) {

        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(
                        message,
                        StompHeaderAccessor.class);

        if (accessor == null) {
            accessor =
                    StompHeaderAccessor.wrap(
                            message);
        }

        StompCommand command =
                accessor.getCommand();

        if (command == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(command)) {
            exigirAuthentication(
                    accessor.getUser());

            return message;
        }

        if (StompCommand.SUBSCRIBE.equals(command)) {
            Authentication authentication =
                    exigirAuthentication(
                            accessor.getUser());

            validarDestinoSuscripcion(
                    authentication,
                    accessor.getDestination());

            return message;
        }

        if (StompCommand.SEND.equals(command)) {
            Authentication authentication =
                    exigirAuthentication(
                            accessor.getUser());

            validarDestinoEnvio(
                    authentication,
                    accessor.getDestination());

            return message;
        }

        return message;
    }

    private void validarDestinoSuscripcion(
            Authentication authentication,
            String destination) {

        if (destination == null
                || destination.isBlank()) {

            throw new AccessDeniedException(
                    "Destino STOMP requerido");
        }

        Matcher conversationMatcher =
                CONVERSATION_TOPIC.matcher(
                        destination);

        if (conversationMatcher.matches()) {
            authorizationService
                    .validarSuscripcion(
                            authentication,
                            parseInteger(
                                    conversationMatcher.group(1),
                                    "Conversacion STOMP invalida"));

            return;
        }

        if (GLOBAL_NOTIFICATION_TOPIC
                .equals(destination)) {

            authorizationService
                    .validarSuscripcionNotificacionesGlobal(
                            authentication);

            return;
        }

        Matcher sedeMatcher =
                SEDE_NOTIFICATION_TOPIC.matcher(
                        destination);

        if (sedeMatcher.matches()) {
            authorizationService
                    .validarSuscripcionNotificacionesSede(
                            authentication,
                            parseInteger(
                                    sedeMatcher.group(1),
                                    "Sede STOMP invalida"));

            return;
        }

        throw new AccessDeniedException(
                "Suscripcion STOMP no permitida");
    }

    private void validarDestinoEnvio(
            Authentication authentication,
            String destination) {

        if (destination == null
                || destination.isBlank()) {

            throw new AccessDeniedException(
                    "Destino STOMP requerido");
        }

        Matcher clientSendMatcher =
                CLIENT_SEND_DESTINATION.matcher(
                        destination);

        if (!clientSendMatcher.matches()) {
            throw new AccessDeniedException(
                    "Envio STOMP no permitido");
        }

        authorizationService
                .validarEnvioCliente(
                        authentication,
                        parseInteger(
                                clientSendMatcher.group(1),
                                "Conversacion STOMP invalida"));
    }

    private Integer parseInteger(
            String value,
            String errorMessage) {

        try {
            return Integer.valueOf(
                    value);
        }
        catch (NumberFormatException ex) {
            throw new AccessDeniedException(
                    errorMessage);
        }
    }

    private Authentication exigirAuthentication(
            Principal principal) {

        if (!(principal instanceof Authentication authentication)
                || !authentication.isAuthenticated()) {

            throw new AccessDeniedException(
                    "WebSocket requiere autenticacion previa en el handshake");
        }

        return authentication;
    }
}