package com.bembosevents.config;

import com.bembosevents.security.chat.ChatStompChannelInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class ChatWebSocketConfig
        implements WebSocketMessageBrokerConfigurer {

    private final ChatStompChannelInterceptor
            chatStompChannelInterceptor;

    public ChatWebSocketConfig(
            ChatStompChannelInterceptor chatStompChannelInterceptor) {

        this.chatStompChannelInterceptor =
                chatStompChannelInterceptor;
    }

    @Override
    public void configureMessageBroker(
            MessageBrokerRegistry registry) {

        registry.enableSimpleBroker(
                "/topic");

        registry.setApplicationDestinationPrefixes(
                "/app");
    }

    @Override
    public void registerStompEndpoints(
            StompEndpointRegistry registry) {

        registry
                .addEndpoint("/ws/chat")
                .setAllowedOriginPatterns(
                        "http://localhost:*",
                        "https://localhost:*",
                        "http://127.0.0.1:*",
                        "https://127.0.0.1:*"
                );
    }

    @Override
    public void configureClientInboundChannel(
            ChannelRegistration registration) {

        registration.interceptors(
                chatStompChannelInterceptor);
    }
}