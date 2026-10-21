package com.bembosevents.service.impl;

import com.bembosevents.dto.chat.ConversacionResumenResponse;
import com.bembosevents.dto.chat.EnviarMensajeChatRequest;
import com.bembosevents.dto.chat.MensajeChatResponse;
import com.bembosevents.entity.Cliente;
import com.bembosevents.entity.Conversacion;
import com.bembosevents.entity.EstadoConversacion;
import com.bembosevents.entity.MensajeChat;
import com.bembosevents.entity.TipoEmisorMensaje;
import com.bembosevents.entity.Usuario;
import com.bembosevents.exception.RecursoNoEncontradoException;
import com.bembosevents.repository.ClienteRepository;
import com.bembosevents.repository.ConversacionRepository;
import com.bembosevents.repository.MensajeChatRepository;
import com.bembosevents.repository.UsuarioRepository;
import com.bembosevents.service.ChatEscalationService;
import com.bembosevents.service.SedeAccessService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ChatEscalationServiceImpl
        implements ChatEscalationService {

    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final ConversacionRepository conversacionRepository;
    private final MensajeChatRepository mensajeChatRepository;
    private final SedeAccessService sedeAccessService;

    public ChatEscalationServiceImpl(
            ClienteRepository clienteRepository,
            UsuarioRepository usuarioRepository,
            ConversacionRepository conversacionRepository,
            MensajeChatRepository mensajeChatRepository,
            SedeAccessService sedeAccessService) {

        this.clienteRepository =
                clienteRepository;

        this.usuarioRepository =
                usuarioRepository;

        this.conversacionRepository =
                conversacionRepository;

        this.mensajeChatRepository =
                mensajeChatRepository;

        this.sedeAccessService =
                sedeAccessService;
    }

    @Override
    public ConversacionResumenResponse solicitarEscalamiento(
            String correoCliente,
            Integer idConversacion) {

        Cliente cliente =
                buscarCliente(
                        correoCliente);

        Conversacion conversacion =
                conversacionRepository
                        .findByIdConversacionAndCliente_IdCliente(
                                idConversacion,
                                cliente.getIdCliente())
                        .orElseThrow(() ->
                                new RecursoNoEncontradoException(
                                        "Conversacion no encontrada"));

        if (conversacion.getSede() == null) {
            throw new IllegalArgumentException(
                    "Debe seleccionar una sede antes de solicitar un encargado");
        }

        if (conversacion.getEstado()
                == EstadoConversacion.CERRADO) {

            throw new IllegalArgumentException(
                    "La conversacion esta cerrada");
        }

        if (conversacion.getEstado()
                == EstadoConversacion.BOT) {

            conversacion.setEstado(
                    EstadoConversacion.ESPERANDO_ENCARGADO);

            conversacion.setFechaActualizacion(
                    LocalDateTime.now());

            conversacion =
                    conversacionRepository.save(
                            conversacion);
        }

        return mapResumen(
                conversacion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversacionResumenResponse> listarPendientes(
            Authentication authentication) {

        if (sedeAccessService
                .esAdministrador(authentication)) {

            return conversacionRepository
                    .findAllByOrderByFechaActualizacionDesc()
                    .stream()
                    .filter(conversacion ->
                            conversacion.getEstado()
                                    == EstadoConversacion.ESPERANDO_ENCARGADO)
                    .map(this::mapResumen)
                    .toList();
        }

        if (sedeAccessService
                .esEncargadoLocal(authentication)) {

            Integer idSede =
                    sedeAccessService
                            .obtenerSedeAsignada(
                                    authentication.getName());

            return conversacionRepository
                    .findBySede_IdSedeAndEstadoInOrderByFechaActualizacionAsc(
                            idSede,
                            List.of(
                                    EstadoConversacion.ESPERANDO_ENCARGADO))
                    .stream()
                    .map(this::mapResumen)
                    .toList();
        }

        throw new AccessDeniedException(
                "No tiene permisos para consultar la cola de chat");
    }

    @Override
    public ConversacionResumenResponse tomarConversacion(
            Integer idConversacion,
            Authentication authentication) {

        exigirEncargadoLocal(
                authentication);

        Conversacion conversacion =
                buscarConversacion(
                        idConversacion);

        validarSede(
                conversacion,
                authentication);

        if (conversacion.getEstado()
                == EstadoConversacion.EN_ATENCION) {

            return mapResumen(
                    conversacion);
        }

        if (conversacion.getEstado()
                != EstadoConversacion.ESPERANDO_ENCARGADO) {

            throw new IllegalArgumentException(
                    "La conversacion no esta esperando un encargado");
        }

        conversacion.setEstado(
                EstadoConversacion.EN_ATENCION);

        conversacion.setFechaActualizacion(
                LocalDateTime.now());

        conversacion =
                conversacionRepository.save(
                        conversacion);

        return mapResumen(
                conversacion);
    }

    @Override
    public MensajeChatResponse enviarMensajeEncargado(
            Integer idConversacion,
            Authentication authentication,
            EnviarMensajeChatRequest request) {

        exigirEncargadoLocal(
                authentication);

        Conversacion conversacion =
                buscarConversacion(
                        idConversacion);

        validarSede(
                conversacion,
                authentication);

        if (conversacion.getEstado()
                != EstadoConversacion.EN_ATENCION) {

            throw new IllegalArgumentException(
                    "La conversacion debe estar EN_ATENCION");
        }

        if (request == null
                || request.contenido() == null
                || request.contenido().isBlank()) {

            throw new IllegalArgumentException(
                    "El mensaje es obligatorio");
        }

        Usuario usuario =
                usuarioRepository
                        .findByCorreoIgnoreCase(
                                authentication.getName())
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Usuario autenticado no encontrado"));

        LocalDateTime ahora =
                LocalDateTime.now();

        MensajeChat mensaje =
                new MensajeChat();

        mensaje.setConversacion(
                conversacion);

        mensaje.setTipoEmisor(
                TipoEmisorMensaje.ENCARGADO_LOCAL);

        mensaje.setUsuarioEmisor(
                usuario);

        mensaje.setContenido(
                request.contenido().strip());

        mensaje.setFechaEnvio(
                ahora);

        mensaje =
                mensajeChatRepository.save(
                        mensaje);

        conversacion.setFechaActualizacion(
                ahora);

        conversacionRepository.save(
                conversacion);

        return new MensajeChatResponse(
                mensaje.getIdMensajeChat(),
                mensaje.getTipoEmisor().name(),
                usuario.getIdUsuario(),
                mensaje.getContenido(),
                mensaje.getFechaEnvio()
        );
    }

    private Cliente buscarCliente(
            String correoCliente) {

        if (correoCliente == null
                || correoCliente.isBlank()) {

            throw new IllegalArgumentException(
                    "Cliente autenticado requerido");
        }

        return clienteRepository
                .findByUsuario_CorreoIgnoreCaseAndActivoTrue(
                        correoCliente.strip())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Perfil Cliente activo no encontrado"));
    }

    private Conversacion buscarConversacion(
            Integer idConversacion) {

        return conversacionRepository
                .findById(
                        idConversacion)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Conversacion no encontrada"));
    }

    private void exigirEncargadoLocal(
            Authentication authentication) {

        if (!sedeAccessService
                .esEncargadoLocal(authentication)) {

            throw new AccessDeniedException(
                    "Solo un ENCARGADO_LOCAL puede tomar o responder la conversacion");
        }
    }

    private void validarSede(
            Conversacion conversacion,
            Authentication authentication) {

        if (conversacion.getSede() == null) {
            throw new AccessDeniedException(
                    "La conversacion no tiene una sede asociada");
        }

        sedeAccessService.validarAccesoSede(
                authentication,
                conversacion.getSede().getIdSede());
    }

    private ConversacionResumenResponse mapResumen(
            Conversacion conversacion) {

        return new ConversacionResumenResponse(
                conversacion.getIdConversacion(),
                conversacion.getSede() == null
                        ? null
                        : conversacion.getSede().getIdSede(),
                conversacion.getEstado().name(),
                conversacion.getFechaCreacion(),
                conversacion.getFechaActualizacion(),
                conversacion.getFechaCierre()
        );
    }
}