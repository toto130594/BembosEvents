package com.bembosevents.service;

import com.bembosevents.dto.chat.EnviarMensajeChatRequest;
import com.bembosevents.entity.Cliente;
import com.bembosevents.entity.Conversacion;
import com.bembosevents.entity.EstadoConversacion;
import com.bembosevents.entity.MensajeChat;
import com.bembosevents.entity.Sede;
import com.bembosevents.entity.Usuario;
import com.bembosevents.repository.ClienteRepository;
import com.bembosevents.repository.ConversacionRepository;
import com.bembosevents.repository.MensajeChatRepository;
import com.bembosevents.repository.UsuarioRepository;
import com.bembosevents.service.impl.ChatEscalationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatEscalationServiceImplTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ConversacionRepository conversacionRepository;

    @Mock
    private MensajeChatRepository mensajeChatRepository;

    @Mock
    private SedeAccessService sedeAccessService;

    @Test
    void clienteEscalaDeBotAEsperandoEncargado() {

        Cliente cliente =
                new Cliente();

        cliente.setIdCliente(5);

        Sede sede =
                new Sede();

        sede.setIdSede(3);

        Conversacion conversacion =
                conversacion(
                        20,
                        cliente,
                        sede,
                        EstadoConversacion.BOT);

        when(
                clienteRepository
                        .findByUsuario_CorreoIgnoreCaseAndActivoTrue(
                                "cliente@test.com")
        ).thenReturn(
                Optional.of(cliente));

        when(
                conversacionRepository
                        .findByIdConversacionAndCliente_IdCliente(
                                20,
                                5)
        ).thenReturn(
                Optional.of(conversacion));

        when(
                conversacionRepository.save(
                        any(Conversacion.class))
        ).thenAnswer(invocation ->
                invocation.getArgument(0));

        ChatEscalationServiceImpl service =
                service();

        var response =
                service.solicitarEscalamiento(
                        "cliente@test.com",
                        20);

        assertEquals(
                "ESPERANDO_ENCARGADO",
                response.estado()
        );

        assertEquals(
                EstadoConversacion.ESPERANDO_ENCARGADO,
                conversacion.getEstado()
        );
    }

    @Test
    void conversacionSinSedeNoPuedeEscalarse() {

        Cliente cliente =
                new Cliente();

        cliente.setIdCliente(5);

        Conversacion conversacion =
                conversacion(
                        20,
                        cliente,
                        null,
                        EstadoConversacion.BOT);

        when(
                clienteRepository
                        .findByUsuario_CorreoIgnoreCaseAndActivoTrue(
                                "cliente@test.com")
        ).thenReturn(
                Optional.of(cliente));

        when(
                conversacionRepository
                        .findByIdConversacionAndCliente_IdCliente(
                                20,
                                5)
        ).thenReturn(
                Optional.of(conversacion));

        ChatEscalationServiceImpl service =
                service();

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.solicitarEscalamiento(
                                "cliente@test.com",
                                20)
        );
    }

    @Test
    void encargadoTomaSoloConversacionDeSuSede() {

        UsernamePasswordAuthenticationToken authentication =
                authEncargado();

        Sede sede =
                new Sede();

        sede.setIdSede(7);

        Conversacion conversacion =
                conversacion(
                        30,
                        null,
                        sede,
                        EstadoConversacion.ESPERANDO_ENCARGADO);

        when(
                sedeAccessService
                        .esEncargadoLocal(
                                authentication)
        ).thenReturn(true);

        when(
                conversacionRepository.findById(30)
        ).thenReturn(
                Optional.of(conversacion));

        when(
                conversacionRepository.save(
                        any(Conversacion.class))
        ).thenAnswer(invocation ->
                invocation.getArgument(0));

        ChatEscalationServiceImpl service =
                service();

        var response =
                service.tomarConversacion(
                        30,
                        authentication);

        assertEquals(
                "EN_ATENCION",
                response.estado()
        );

        verify(
                sedeAccessService
        ).validarAccesoSede(
                authentication,
                7);
    }

    @Test
    void noEncargadoNoPuedeTomarConversacion() {

        UsernamePasswordAuthenticationToken authentication =
                UsernamePasswordAuthenticationToken.authenticated(
                        "admin@test.com",
                        null,
                        List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_ADMINISTRADOR")
                        )
                );

        when(
                sedeAccessService
                        .esEncargadoLocal(
                                authentication)
        ).thenReturn(false);

        ChatEscalationServiceImpl service =
                service();

        assertThrows(
                AccessDeniedException.class,
                () ->
                        service.tomarConversacion(
                                30,
                                authentication)
        );
    }

    @Test
    void encargadoRespondeSoloEnAtencion() {

        UsernamePasswordAuthenticationToken authentication =
                authEncargado();

        Sede sede =
                new Sede();

        sede.setIdSede(7);

        Usuario usuario =
                new Usuario();

        usuario.setIdUsuario(99);

        Conversacion conversacion =
                conversacion(
                        30,
                        null,
                        sede,
                        EstadoConversacion.EN_ATENCION);

        when(
                sedeAccessService
                        .esEncargadoLocal(
                                authentication)
        ).thenReturn(true);

        when(
                conversacionRepository.findById(30)
        ).thenReturn(
                Optional.of(conversacion));

        when(
                usuarioRepository
                        .findByCorreoIgnoreCase(
                                "encargado@test.com")
        ).thenReturn(
                Optional.of(usuario));

        when(
                mensajeChatRepository.save(
                        any(MensajeChat.class))
        ).thenAnswer(invocation -> {
            MensajeChat mensaje =
                    invocation.getArgument(0);

            mensaje.setIdMensajeChat(77);

            return mensaje;
        });

        ChatEscalationServiceImpl service =
                service();

        var response =
                service.enviarMensajeEncargado(
                        30,
                        authentication,
                        new EnviarMensajeChatRequest(
                                "Hola, te ayudo con tu evento")
                );

        assertEquals(
                "ENCARGADO_LOCAL",
                response.tipoEmisor()
        );

        assertEquals(
                99,
                response.idUsuarioEmisor()
        );

        verify(
                sedeAccessService
        ).validarAccesoSede(
                authentication,
                7);
    }

    private ChatEscalationServiceImpl service() {

        return new ChatEscalationServiceImpl(
                clienteRepository,
                usuarioRepository,
                conversacionRepository,
                mensajeChatRepository,
                sedeAccessService
        );
    }

    private UsernamePasswordAuthenticationToken authEncargado() {

        return UsernamePasswordAuthenticationToken.authenticated(
                "encargado@test.com",
                null,
                List.of(
                        new SimpleGrantedAuthority(
                                "ROLE_ENCARGADO_LOCAL")
                )
        );
    }

    private Conversacion conversacion(
            Integer id,
            Cliente cliente,
            Sede sede,
            EstadoConversacion estado) {

        Conversacion conversacion =
                new Conversacion();

        conversacion.setIdConversacion(id);
        conversacion.setCliente(cliente);
        conversacion.setSede(sede);
        conversacion.setEstado(estado);
        conversacion.setFechaCreacion(
                LocalDateTime.of(
                        2026,
                        10,
                        21,
                        10,
                        0)
        );
        conversacion.setFechaActualizacion(
                LocalDateTime.of(
                        2026,
                        10,
                        21,
                        10,
                        0)
        );

        return conversacion;
    }
}