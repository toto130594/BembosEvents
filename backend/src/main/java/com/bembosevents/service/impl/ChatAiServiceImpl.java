package com.bembosevents.service.impl;

import com.bembosevents.ai.orchestration.AiConversationOrchestrator;
import com.bembosevents.ai.orchestration.AiConversationRequest;
import com.bembosevents.dto.chat.ConversacionDetalleResponse;
import com.bembosevents.dto.chat.MensajeChatResponse;
import com.bembosevents.entity.Conversacion;
import com.bembosevents.entity.MensajeChat;
import com.bembosevents.entity.TipoEmisorMensaje;
import com.bembosevents.exception.RecursoNoEncontradoException;
import com.bembosevents.repository.ConversacionRepository;
import com.bembosevents.repository.MensajeChatRepository;
import com.bembosevents.service.ChatAiService;
import com.bembosevents.service.ChatClienteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class ChatAiServiceImpl
        implements ChatAiService {

    private static final int MAX_CONTENT_LENGTH = 4000;

    private final AiConversationOrchestrator orchestrator;
    private final ChatClienteService chatClienteService;
    private final ConversacionRepository conversacionRepository;
    private final MensajeChatRepository mensajeChatRepository;

    public ChatAiServiceImpl(
            AiConversationOrchestrator orchestrator,
            ChatClienteService chatClienteService,
            ConversacionRepository conversacionRepository,
            MensajeChatRepository mensajeChatRepository) {

        this.orchestrator =
                orchestrator;

        this.chatClienteService =
                chatClienteService;

        this.conversacionRepository =
                conversacionRepository;

        this.mensajeChatRepository =
                mensajeChatRepository;
    }

    @Override
    public Optional<MensajeChatResponse> responder(
            String correoCliente,
            Integer idConversacion,
            String mensajeCliente) {

        if (correoCliente == null
                || correoCliente.isBlank()) {
            throw new IllegalArgumentException(
                    "El correo del cliente es obligatorio");
        }

        if (idConversacion == null) {
            throw new IllegalArgumentException(
                    "La conversacion es obligatoria");
        }

        if (mensajeCliente == null
                || mensajeCliente.isBlank()) {
            throw new IllegalArgumentException(
                    "El mensaje del cliente es obligatorio");
        }

        ConversacionDetalleResponse detalle =
                chatClienteService.obtenerPropia(
                        correoCliente,
                        idConversacion);

        if (!"BOT".equals(
                detalle.estado())) {

            return Optional.empty();
        }

        Conversacion conversacion =
                conversacionRepository
                        .findById(
                                idConversacion)
                        .orElseThrow(() ->
                                new RecursoNoEncontradoException(
                                        "Conversacion no encontrada"));

        String respuesta =
                orchestrator.responder(
                        new AiConversationRequest(
                                correoCliente,
                                idConversacion,
                                detalle.idSede(),
                                mensajeCliente));

        if (respuesta == null
                || respuesta.isBlank()) {
            throw new IllegalStateException(
                    "El orquestador IA devolvio una respuesta vacia");
        }

        respuesta =
                respuesta.strip();

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

        mensaje.setConversacion(
                conversacion);

        mensaje.setTipoEmisor(
                TipoEmisorMensaje.IA);

        mensaje.setUsuarioEmisor(
                null);

        mensaje.setContenido(
                respuesta);

        mensaje.setFechaEnvio(
                ahora);

        mensaje =
                mensajeChatRepository.save(
                        mensaje);

        conversacion.setFechaActualizacion(
                ahora);

        conversacionRepository.save(
                conversacion);

        return Optional.of(
                mapMensaje(
                        mensaje));
    }

    private MensajeChatResponse mapMensaje(
            MensajeChat mensaje) {

        return new MensajeChatResponse(
                mensaje.getIdMensajeChat(),
                mensaje.getTipoEmisor().name(),
                null,
                mensaje.getContenido(),
                mensaje.getFechaEnvio());
    }
}