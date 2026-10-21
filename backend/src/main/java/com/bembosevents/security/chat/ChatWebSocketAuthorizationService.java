package com.bembosevents.security.chat;

import com.bembosevents.entity.Cliente;
import com.bembosevents.entity.Conversacion;
import com.bembosevents.repository.ClienteRepository;
import com.bembosevents.repository.ConversacionRepository;
import com.bembosevents.service.SedeAccessService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ChatWebSocketAuthorizationService {

    private static final String ROLE_CLIENTE =
            "ROLE_CLIENTE";

    private final ClienteRepository clienteRepository;
    private final ConversacionRepository conversacionRepository;
    private final SedeAccessService sedeAccessService;

    public ChatWebSocketAuthorizationService(
            ClienteRepository clienteRepository,
            ConversacionRepository conversacionRepository,
            SedeAccessService sedeAccessService) {

        this.clienteRepository =
                clienteRepository;

        this.conversacionRepository =
                conversacionRepository;

        this.sedeAccessService =
                sedeAccessService;
    }

    public void validarSuscripcion(
            Authentication authentication,
            Integer idConversacion) {

        validarAutenticado(authentication);

        if (sedeAccessService
                .esAdministrador(authentication)) {

            exigirConversacion(idConversacion);
            return;
        }

        if (sedeAccessService
                .esEncargadoLocal(authentication)) {

            Conversacion conversacion =
                    exigirConversacion(
                            idConversacion);

            if (conversacion.getSede() == null) {
                throw new AccessDeniedException(
                        "Conversacion fuera del alcance de la sede");
            }

            sedeAccessService.validarAccesoSede(
                    authentication,
                    conversacion.getSede().getIdSede());

            return;
        }

        if (tieneRol(
                authentication,
                ROLE_CLIENTE)) {

            Cliente cliente =
                    clienteRepository
                            .findByUsuario_CorreoIgnoreCaseAndActivoTrue(
                                    authentication.getName())
                            .orElseThrow(() ->
                                    new AccessDeniedException(
                                            "Perfil Cliente no disponible"));

            conversacionRepository
                    .findByIdConversacionAndCliente_IdCliente(
                            idConversacion,
                            cliente.getIdCliente())
                    .orElseThrow(() ->
                            new AccessDeniedException(
                                    "Conversacion fuera del alcance del cliente"));

            return;
        }

        throw new AccessDeniedException(
                "Rol no autorizado para chat realtime");
    }

    public void validarEnvioCliente(
            Authentication authentication,
            Integer idConversacion) {

        validarAutenticado(authentication);

        if (!tieneRol(
                authentication,
                ROLE_CLIENTE)) {

            throw new AccessDeniedException(
                    "Solo CLIENTE puede enviar mensajes");
        }

        Cliente cliente =
                clienteRepository
                        .findByUsuario_CorreoIgnoreCaseAndActivoTrue(
                                authentication.getName())
                        .orElseThrow(() ->
                                new AccessDeniedException(
                                        "Perfil Cliente no disponible"));

        conversacionRepository
                .findByIdConversacionAndCliente_IdCliente(
                        idConversacion,
                        cliente.getIdCliente())
                .orElseThrow(() ->
                        new AccessDeniedException(
                                "Conversacion fuera del alcance del cliente"));
    }

    public void validarSuscripcionNotificacionesGlobal(
            Authentication authentication) {

        validarAutenticado(authentication);

        if (!sedeAccessService
                .esAdministrador(authentication)) {

            throw new AccessDeniedException(
                    "Solo ADMINISTRADOR puede suscribirse a notificaciones globales");
        }
    }

    public void validarSuscripcionNotificacionesSede(
            Authentication authentication,
            Integer idSede) {

        validarAutenticado(authentication);

        if (idSede == null) {
            throw new AccessDeniedException(
                    "Sede requerida para notificaciones");
        }

        if (sedeAccessService
                .esAdministrador(authentication)) {

            return;
        }

        if (!sedeAccessService
                .esEncargadoLocal(authentication)) {

            throw new AccessDeniedException(
                    "Rol no autorizado para notificaciones BackOffice");
        }

        sedeAccessService.validarAccesoSede(
                authentication,
                idSede);
    }

    private Conversacion exigirConversacion(
            Integer idConversacion) {

        return conversacionRepository
                .findById(idConversacion)
                .orElseThrow(() ->
                        new AccessDeniedException(
                                "Conversacion no accesible"));
    }

    private void validarAutenticado(
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || authentication.getName().isBlank()) {

            throw new AccessDeniedException(
                    "Autenticacion WebSocket requerida");
        }
    }

    private boolean tieneRol(
            Authentication authentication,
            String rol) {

        return authentication
                .getAuthorities()
                .stream()
                .anyMatch(authority ->
                        rol.equals(
                                authority.getAuthority())
                );
    }
}