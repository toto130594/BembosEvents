package com.bembosevents.service.impl;

import com.bembosevents.dto.chat.ConversacionDetalleResponse;
import com.bembosevents.dto.chat.ConversacionResumenResponse;
import com.bembosevents.dto.chat.MensajeChatResponse;
import com.bembosevents.entity.Conversacion;
import com.bembosevents.entity.MensajeChat;
import com.bembosevents.exception.RecursoNoEncontradoException;
import com.bembosevents.repository.ConversacionRepository;
import com.bembosevents.repository.MensajeChatRepository;
import com.bembosevents.service.ChatBackofficeService;
import com.bembosevents.service.SedeAccessService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ChatBackofficeServiceImpl
        implements ChatBackofficeService {

    private final ConversacionRepository conversacionRepository;
    private final MensajeChatRepository mensajeChatRepository;
    private final SedeAccessService sedeAccessService;

    public ChatBackofficeServiceImpl(
            ConversacionRepository conversacionRepository,
            MensajeChatRepository mensajeChatRepository,
            SedeAccessService sedeAccessService) {

        this.conversacionRepository = conversacionRepository;
        this.mensajeChatRepository = mensajeChatRepository;
        this.sedeAccessService = sedeAccessService;
    }

    @Override
    public List<ConversacionResumenResponse> listarAccesibles(
            Authentication authentication) {

        if (sedeAccessService
                .esAdministrador(authentication)) {

            return conversacionRepository
                    .findAllByOrderByFechaActualizacionDesc()
                    .stream()
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
                    .findBySede_IdSedeOrderByFechaActualizacionDesc(
                            idSede)
                    .stream()
                    .map(this::mapResumen)
                    .toList();
        }

        throw new AccessDeniedException(
                "No tiene permisos para acceder al chat de BackOffice");
    }

    @Override
    public ConversacionDetalleResponse obtenerAccesible(
            Integer idConversacion,
            Authentication authentication) {

        Conversacion conversacion =
                conversacionRepository
                        .findById(idConversacion)
                        .orElseThrow(() ->
                                new RecursoNoEncontradoException(
                                        "Conversacion no encontrada"));

        if (sedeAccessService
                .esAdministrador(authentication)) {

            return mapDetalle(conversacion);
        }

        if (!sedeAccessService
                .esEncargadoLocal(authentication)) {

            throw new AccessDeniedException(
                    "No tiene permisos para acceder al chat de BackOffice");
        }

        if (conversacion.getSede() == null) {
            throw new AccessDeniedException(
                    "La conversacion no esta asociada a una sede accesible");
        }

        sedeAccessService.validarAccesoSede(
                authentication,
                conversacion.getSede().getIdSede());

        return mapDetalle(conversacion);
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
            Conversacion conversacion) {

        List<MensajeChat> mensajes =
                mensajeChatRepository
                        .findByConversacion_IdConversacionOrderByFechaEnvioAscIdMensajeChatAsc(
                                conversacion.getIdConversacion());

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