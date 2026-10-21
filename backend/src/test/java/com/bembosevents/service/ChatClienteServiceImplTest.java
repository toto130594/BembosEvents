package com.bembosevents.service;

import com.bembosevents.dto.chat.ConversacionDetalleResponse;
import com.bembosevents.dto.chat.CrearConversacionRequest;
import com.bembosevents.entity.Cliente;
import com.bembosevents.entity.Conversacion;
import com.bembosevents.entity.EstadoConversacion;
import com.bembosevents.entity.Usuario;
import com.bembosevents.exception.RecursoNoEncontradoException;
import com.bembosevents.repository.ClienteRepository;
import com.bembosevents.repository.ConversacionRepository;
import com.bembosevents.repository.MensajeChatRepository;
import com.bembosevents.repository.SedeRepository;
import com.bembosevents.service.impl.ChatClienteServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatClienteServiceImplTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private SedeRepository sedeRepository;

    @Mock
    private ConversacionRepository conversacionRepository;

    @Mock
    private MensajeChatRepository mensajeChatRepository;

    @Test
    void crearSinSedeIniciaEnBot() {

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(7);

        Cliente cliente = new Cliente();
        cliente.setIdCliente(3);
        cliente.setUsuario(usuario);

        when(
                clienteRepository
                        .findByUsuario_CorreoIgnoreCaseAndActivoTrue(
                                "cliente@test.com")
        ).thenReturn(Optional.of(cliente));

        when(
                conversacionRepository.save(
                        any(Conversacion.class))
        ).thenAnswer(invocation -> {
            Conversacion conversacion =
                    invocation.getArgument(0);

            conversacion.setIdConversacion(11);

            return conversacion;
        });

        ChatClienteServiceImpl service =
                new ChatClienteServiceImpl(
                        clienteRepository,
                        sedeRepository,
                        conversacionRepository,
                        mensajeChatRepository
                );

        ConversacionDetalleResponse response =
                service.crear(
                        "cliente@test.com",
                        new CrearConversacionRequest(null)
                );

        assertEquals(11, response.idConversacion());
        assertEquals(
                EstadoConversacion.BOT.name(),
                response.estado()
        );
        assertNull(response.idSede());
    }

    @Test
    void obtenerPropiaNoPermiteConversacionAjena() {

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(7);

        Cliente cliente = new Cliente();
        cliente.setIdCliente(3);
        cliente.setUsuario(usuario);

        when(
                clienteRepository
                        .findByUsuario_CorreoIgnoreCaseAndActivoTrue(
                                "cliente@test.com")
        ).thenReturn(Optional.of(cliente));

        when(
                conversacionRepository
                        .findByIdConversacionAndCliente_IdCliente(
                                99,
                                3)
        ).thenReturn(Optional.empty());

        ChatClienteServiceImpl service =
                new ChatClienteServiceImpl(
                        clienteRepository,
                        sedeRepository,
                        conversacionRepository,
                        mensajeChatRepository
                );

        assertThrows(
                RecursoNoEncontradoException.class,
                () ->
                        service.obtenerPropia(
                                "cliente@test.com",
                                99)
        );
    }
}