package com.bembosevents.service.impl;

import com.bembosevents.dto.chat.ConversacionDetalleResponse;
import com.bembosevents.dto.chat.ConversacionResumenResponse;
import com.bembosevents.dto.chat.CrearConversacionRequest;
import com.bembosevents.dto.chat.EnviarMensajeChatRequest;
import com.bembosevents.dto.chat.MensajeChatResponse;
import com.bembosevents.entity.Cliente;
import com.bembosevents.entity.Conversacion;
import com.bembosevents.entity.EstadoConversacion;
import com.bembosevents.entity.MensajeChat;
import com.bembosevents.entity.Sede;
import com.bembosevents.entity.TipoEmisorMensaje;
import com.bembosevents.exception.RecursoNoEncontradoException;
import com.bembosevents.repository.ClienteRepository;
import com.bembosevents.repository.ConversacionRepository;
import com.bembosevents.repository.MensajeChatRepository;
import com.bembosevents.repository.SedeRepository;
import com.bembosevents.service.ChatClienteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ChatClienteServiceImpl
        implements ChatClienteService {

    private final ClienteRepository clienteRepository;
    private final SedeRepository sedeRepository;
    private final ConversacionRepository conversacionRepository;
    private final MensajeChatRepository mensajeChatRepository;

    public ChatClienteServiceImpl(
            ClienteRepository clienteRepository,
            SedeRepository sedeRepository,
            ConversacionRepository conversacionRepository,
            MensajeChatRepository mensajeChatRepository) {

        this.clienteRepository = clienteRepository;
        this.sedeRepository = sedeRepository;
        this.conversacionRepository = conversacionRepository;
        this.mensajeChatRepository = mensajeChatRepository;
    }

    @Override
    public ConversacionDetalleResponse crear(
            String correoCliente,
            CrearConversacionRequest request) {

        Cliente cliente =
                buscarClienteAutenticado(correoCliente);

        Sede sede = null;

        if (request.idSede() != null) {
            sede = sedeRepository
                    .findByIdSedeAndActivoTrue(
                            request.idSede())
                    .orElseThrow(() ->
                            new RecursoNoEncontradoException(
                                    "Sede no encontrada"));
        }

        LocalDateTime ahora =
                LocalDateTime.now();

        Conversacion conversacion =
                new Conversacion();

        conversacion.setCliente(cliente);
        conversacion.setSede(sede);
        conversacion.setEstado(
                EstadoConversacion.BOT);
        conversacion.setFechaCreacion(ahora);
        conversacion.setFechaActualizacion(ahora);
        conversacion.setFechaCierre(null);

        conversacion =
                conversacionRepository.save(
                        conversacion);

        return mapDetalle(
                conversacion,
                List.of());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversacionResumenResponse> listarPropias(
            String correoCliente) {

        Cliente cliente =
                buscarClienteAutenticado(correoCliente);

        return conversacionRepository
                .findByCliente_IdClienteOrderByFechaActualizacionDesc(
                        cliente.getIdCliente())
                .stream()
                .map(this::mapResumen)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ConversacionDetalleResponse obtenerPropia(
            String correoCliente,
            Integer idConversacion) {

        Cliente cliente =
                buscarClienteAutenticado(correoCliente);

        Conversacion conversacion =
                conversacionRepository
                        .findByIdConversacionAndCliente_IdCliente(
                                idConversacion,
                                cliente.getIdCliente())
                        .orElseThrow(() ->
                                new RecursoNoEncontradoException(
                                        "Conversacion no encontrada"));

        return mapDetalle(
                conversacion,
                mensajesDe(conversacion));
    }

    @Override
    public MensajeChatResponse enviarMensaje(
            String correoCliente,
            Integer idConversacion,
            EnviarMensajeChatRequest request) {

        Cliente cliente =
                buscarClienteAutenticado(correoCliente);

        Conversacion conversacion =
                conversacionRepository
                        .findByIdConversacionAndCliente_IdCliente(
                                idConversacion,
                                cliente.getIdCliente())
                        .orElseThrow(() ->
                                new RecursoNoEncontradoException(
                                        "Conversacion no encontrada"));

        if (conversacion.getEstado()
                == EstadoConversacion.CERRADO) {

            throw new IllegalArgumentException(
                    "La conversacion esta cerrada");
        }

        LocalDateTime ahora =
                LocalDateTime.now();

        MensajeChat mensaje =
                new MensajeChat();

        mensaje.setConversacion(conversacion);
        mensaje.setTipoEmisor(
                TipoEmisorMensaje.CLIENTE);
        mensaje.setUsuarioEmisor(
                cliente.getUsuario());
        mensaje.setContenido(
                request.contenido().strip());
        mensaje.setFechaEnvio(ahora);

        mensaje =
                mensajeChatRepository.save(
                        mensaje);

        conversacion.setFechaActualizacion(
                ahora);

        conversacionRepository.save(
                conversacion);

        return mapMensaje(mensaje);
    }

    private Cliente buscarClienteAutenticado(
            String correoCliente) {

        if (correoCliente == null
                || correoCliente.isBlank()) {

            throw new IllegalArgumentException(
                    "El cliente autenticado es obligatorio");
        }

        Cliente cliente =
                clienteRepository
                        .findByUsuario_CorreoIgnoreCaseAndActivoTrue(
                                correoCliente.strip())
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "El usuario autenticado no tiene un perfil Cliente activo"));

        if (cliente.getUsuario() == null) {
            throw new IllegalStateException(
                    "El perfil Cliente no tiene un Usuario asociado");
        }

        return cliente;
    }

    private List<MensajeChat> mensajesDe(
            Conversacion conversacion) {

        return mensajeChatRepository
                .findByConversacion_IdConversacionOrderByFechaEnvioAscIdMensajeChatAsc(
                        conversacion.getIdConversacion());
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

    private ConversacionDetalleResponse mapDetalle(
            Conversacion conversacion,
            List<MensajeChat> mensajes) {

        return new ConversacionDetalleResponse(
                conversacion.getIdConversacion(),
                conversacion.getCliente().getIdCliente(),
                conversacion.getSede() == null
                        ? null
                        : conversacion.getSede().getIdSede(),
                conversacion.getEstado().name(),
                conversacion.getFechaCreacion(),
                conversacion.getFechaActualizacion(),
                conversacion.getFechaCierre(),
                mensajes.stream()
                        .map(this::mapMensaje)
                        .toList()
        );
    }

    private MensajeChatResponse mapMensaje(
            MensajeChat mensaje) {

        return new MensajeChatResponse(
                mensaje.getIdMensajeChat(),
                mensaje.getTipoEmisor().name(),
                mensaje.getUsuarioEmisor() == null
                        ? null
                        : mensaje.getUsuarioEmisor().getIdUsuario(),
                mensaje.getContenido(),
                mensaje.getFechaEnvio()
        );
    }
}