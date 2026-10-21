package com.bembosevents.service.impl;

import com.bembosevents.ai.ChatAiProvider;
import com.bembosevents.ai.ChatAiRequest;
import com.bembosevents.dto.chat.MensajeChatResponse;
import com.bembosevents.entity.Conversacion;
import com.bembosevents.entity.EstadoConversacion;
import com.bembosevents.entity.MensajeChat;
import com.bembosevents.entity.TipoEmisorMensaje;
import com.bembosevents.exception.RecursoNoEncontradoException;
import com.bembosevents.repository.ConversacionRepository;
import com.bembosevents.repository.MensajeChatRepository;
import com.bembosevents.service.ChatAiService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class ChatAiServiceImpl
        implements ChatAiService {

    private static final int MAX_CONTENT_LENGTH = 4000;

    private final ChatAiProvider chatAiProvider;
    private final ConversacionRepository conversacionRepository;
    private final MensajeChatRepository mensajeChatRepository;

    public ChatAiServiceImpl(
            ChatAiProvider chatAiProvider,
            ConversacionRepository conversacionRepository,
            MensajeChatRepository mensajeChatRepository) {

        this.chatAiProvider = chatAiProvider;
        this.conversacionRepository = conversacionRepository;
        this.mensajeChatRepository = mensajeChatRepository;
    }

    @Override
    public Optional<MensajeChatResponse> responder(
            Integer idConversacion,
            String mensajeCliente) {

        if (idConversacion == null) {
            throw new IllegalArgumentException(
                    "La conversacion es obligatoria");
        }

        if (mensajeCliente == null
                || mensajeCliente.isBlank()) {

            throw new IllegalArgumentException(
                    "El mensaje del cliente es obligatorio");
        }

        Conversacion conversacion =
                conversacionRepository
                        .findById(idConversacion)
                        .orElseThrow(() ->
                                new RecursoNoEncontradoException(
                                        "Conversacion no encontrada"));

        if (conversacion.getEstado()
                != EstadoConversacion.BOT) {

            return Optional.empty();
        }

        String respuesta =
                chatAiProvider.responder(
                        new ChatAiRequest(
                                conversacion.getIdConversacion(),
                                conversacion.getSede() == null
                                        ? null
                                        : conversacion.getSede().getIdSede(),
                                mensajeCliente.strip()
                        )
                );

        if (respuesta == null
                || respuesta.isBlank()) {

            throw new IllegalStateException(
                    "El proveedor IA devolvio una respuesta vacia");
        }

        respuesta = respuesta.strip();

        if (respuesta.length()
                > MAX_CONTENT_LENGTH) {

            respuesta =
                    respuesta.substring(
                            0,
                            MAX_CONTENT_LENGTH);
        }

        LocalDateTime ahora =
                LocalDateTime.now();

        MensajeChat mensaje =
                new MensajeChat();

        mensaje.setConversacion(conversacion);
        mensaje.setTipoEmisor(
                TipoEmisorMensaje.IA);
        mensaje.setUsuarioEmisor(null);
        mensaje.setContenido(respuesta);
        mensaje.setFechaEnvio(ahora);

        mensaje =
                mensajeChatRepository.save(
                        mensaje);

        conversacion.setFechaActualizacion(
                ahora);

        conversacionRepository.save(
                conversacion);

        return Optional.of(
                mapMensaje(mensaje));
    }

    private MensajeChatResponse mapMensaje(
            MensajeChat mensaje) {

        return new MensajeChatResponse(
                mensaje.getIdMensajeChat(),
                mensaje.getTipoEmisor().name(),
                null,
                mensaje.getContenido(),
                mensaje.getFechaEnvio()
        );
    }
}