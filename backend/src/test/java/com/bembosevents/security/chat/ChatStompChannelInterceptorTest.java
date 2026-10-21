package com.bembosevents.security.chat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ChatStompChannelInterceptorTest {

    @Mock
    private ChatWebSocketAuthorizationService
            authorizationService;

    @Mock
    private MessageChannel channel;

    @Test
    void connectSinAutenticacionEsDenegado() {

        ChatStompChannelInterceptor interceptor =
                new ChatStompChannelInterceptor(
                        authorizationService);

        Message<byte[]> message =
                message(
                        StompCommand.CONNECT,
                        null,
                        null);

        assertThrows(
                AccessDeniedException.class,
                () ->
                        interceptor.preSend(
                                message,
                                channel)
        );
    }

    @Test
    void sendDirectoATopicEsDenegado() {

        ChatStompChannelInterceptor interceptor =
                new ChatStompChannelInterceptor(
                        authorizationService);

        Message<byte[]> message =
                message(
                        StompCommand.SEND,
                        "/topic/chat/conversaciones/12",
                        cliente());

        assertThrows(
                AccessDeniedException.class,
                () ->
                        interceptor.preSend(
                                message,
                                channel)
        );
    }

    @Test
    void sendDirectoANotificacionesEsDenegado() {

        ChatStompChannelInterceptor interceptor =
                new ChatStompChannelInterceptor(
                        authorizationService);

        Message<byte[]> message =
                message(
                        StompCommand.SEND,
                        "/topic/backoffice/chat/notificaciones",
                        cliente());

        assertThrows(
                AccessDeniedException.class,
                () ->
                        interceptor.preSend(
                                message,
                                channel)
        );
    }

    @Test
    void sendClientePorAppValidaOwnership() {

        var authentication =
                cliente();

        ChatStompChannelInterceptor interceptor =
                new ChatStompChannelInterceptor(
                        authorizationService);

        Message<byte[]> message =
                message(
                        StompCommand.SEND,
                        "/app/chat/conversaciones/12/mensajes",
                        authentication);

        assertDoesNotThrow(() ->
                interceptor.preSend(
                        message,
                        channel)
        );

        verify(
                authorizationService
        ).validarEnvioCliente(
                authentication,
                12);
    }

    @Test
    void sendAOtroAppDestinationEsDenegado() {

        ChatStompChannelInterceptor interceptor =
                new ChatStompChannelInterceptor(
                        authorizationService);

        Message<byte[]> message =
                message(
                        StompCommand.SEND,
                        "/app/chat/admin/fake",
                        cliente());

        assertThrows(
                AccessDeniedException.class,
                () ->
                        interceptor.preSend(
                                message,
                                channel)
        );
    }

    private Message<byte[]> message(
            StompCommand command,
            String destination,
            UsernamePasswordAuthenticationToken authentication) {

        StompHeaderAccessor accessor =
                StompHeaderAccessor.create(
                        command);

        if (destination != null) {
            accessor.setDestination(
                    destination);
        }

        if (authentication != null) {
            accessor.setUser(
                    authentication);
        }

        return MessageBuilder.createMessage(
                new byte[0],
                accessor.getMessageHeaders()
        );
    }

    private UsernamePasswordAuthenticationToken cliente() {

        return UsernamePasswordAuthenticationToken.authenticated(
                "cliente@test.com",
                null,
                List.of(
                        new SimpleGrantedAuthority(
                                "ROLE_CLIENTE")
                )
        );
    }
}