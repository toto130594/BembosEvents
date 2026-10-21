package com.bembosevents.service;

import com.bembosevents.entity.Cliente;
import com.bembosevents.entity.Conversacion;
import com.bembosevents.entity.EstadoConversacion;
import com.bembosevents.entity.Sede;
import com.bembosevents.repository.ConversacionRepository;
import com.bembosevents.repository.MensajeChatRepository;
import com.bembosevents.service.impl.ChatBackofficeServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatBackofficeServiceImplTest {

    @Mock
    private ConversacionRepository conversacionRepository;

    @Mock
    private MensajeChatRepository mensajeChatRepository;

    @Mock
    private SedeAccessService sedeAccessService;

    @Test
    void administradorListaTodasLasConversaciones() {

        UsernamePasswordAuthenticationToken authentication =
                UsernamePasswordAuthenticationToken.authenticated(
                        "admin@test.com",
                        null,
                        List.of()
                );

        when(
                sedeAccessService.esAdministrador(
                        authentication)
        ).thenReturn(true);

        when(
                conversacionRepository
                        .findAllByOrderByFechaActualizacionDesc()
        ).thenReturn(List.of());

        ChatBackofficeServiceImpl service =
                new ChatBackofficeServiceImpl(
                        conversacionRepository,
                        mensajeChatRepository,
                        sedeAccessService
                );

        assertEquals(
                0,
                service.listarAccesibles(
                        authentication).size()
        );

        verify(
                conversacionRepository
        ).findAllByOrderByFechaActualizacionDesc();
    }

    @Test
    void encargadoListaSoloSuSede() {

        UsernamePasswordAuthenticationToken authentication =
                UsernamePasswordAuthenticationToken.authenticated(
                        "encargado@test.com",
                        null,
                        List.of()
                );

        when(
                sedeAccessService.esAdministrador(
                        authentication)
        ).thenReturn(false);

        when(
                sedeAccessService.esEncargadoLocal(
                        authentication)
        ).thenReturn(true);

        when(
                sedeAccessService.obtenerSedeAsignada(
                        "encargado@test.com")
        ).thenReturn(5);

        when(
                conversacionRepository
                        .findBySede_IdSedeOrderByFechaActualizacionDesc(
                                5)
        ).thenReturn(List.of());

        ChatBackofficeServiceImpl service =
                new ChatBackofficeServiceImpl(
                        conversacionRepository,
                        mensajeChatRepository,
                        sedeAccessService
                );

        assertEquals(
                0,
                service.listarAccesibles(
                        authentication).size()
        );

        verify(
                conversacionRepository
        ).findBySede_IdSedeOrderByFechaActualizacionDesc(
                5);
    }

    @Test
    void encargadoNoAccedeConversacionSinSede() {

        UsernamePasswordAuthenticationToken authentication =
                UsernamePasswordAuthenticationToken.authenticated(
                        "encargado@test.com",
                        null,
                        List.of()
                );

        Cliente cliente = new Cliente();
        cliente.setIdCliente(1);

        Conversacion conversacion =
                new Conversacion();

        conversacion.setIdConversacion(20);
        conversacion.setCliente(cliente);
        conversacion.setSede(null);
        conversacion.setEstado(
                EstadoConversacion.BOT);
        conversacion.setFechaCreacion(
                LocalDateTime.now());
        conversacion.setFechaActualizacion(
                LocalDateTime.now());

        when(
                conversacionRepository.findById(20)
        ).thenReturn(Optional.of(conversacion));

        when(
                sedeAccessService.esAdministrador(
                        authentication)
        ).thenReturn(false);

        when(
                sedeAccessService.esEncargadoLocal(
                        authentication)
        ).thenReturn(true);

        ChatBackofficeServiceImpl service =
                new ChatBackofficeServiceImpl(
                        conversacionRepository,
                        mensajeChatRepository,
                        sedeAccessService
                );

        assertThrows(
                AccessDeniedException.class,
                () ->
                        service.obtenerAccesible(
                                20,
                                authentication)
        );
    }
}