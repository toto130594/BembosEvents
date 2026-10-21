package com.bembosevents.security.chat;

import com.bembosevents.entity.Cliente;
import com.bembosevents.entity.Conversacion;
import com.bembosevents.entity.Sede;
import com.bembosevents.repository.ClienteRepository;
import com.bembosevents.repository.ConversacionRepository;
import com.bembosevents.service.SedeAccessService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatWebSocketAuthorizationServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ConversacionRepository conversacionRepository;

    @Mock
    private SedeAccessService sedeAccessService;

    @Test
    void clientePuedeSuscribirseASuConversacion() {

        UsernamePasswordAuthenticationToken authentication =
                auth(
                        "cliente@test.com",
                        "ROLE_CLIENTE"
                );

        Cliente cliente =
                new Cliente();

        cliente.setIdCliente(7);

        Conversacion conversacion =
                new Conversacion();

        conversacion.setIdConversacion(20);
        conversacion.setCliente(cliente);

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
                                7)
        ).thenReturn(
                Optional.of(conversacion));

        ChatWebSocketAuthorizationService service =
                service();

        assertDoesNotThrow(() ->
                service.validarSuscripcion(
                        authentication,
                        20)
        );
    }

    @Test
    void clienteNoPuedeSuscribirseAConversacionAjena() {

        UsernamePasswordAuthenticationToken authentication =
                auth(
                        "cliente@test.com",
                        "ROLE_CLIENTE"
                );

        Cliente cliente =
                new Cliente();

        cliente.setIdCliente(7);

        when(
                clienteRepository
                        .findByUsuario_CorreoIgnoreCaseAndActivoTrue(
                                "cliente@test.com")
        ).thenReturn(
                Optional.of(cliente));

        when(
                conversacionRepository
                        .findByIdConversacionAndCliente_IdCliente(
                                99,
                                7)
        ).thenReturn(
                Optional.empty());

        ChatWebSocketAuthorizationService service =
                service();

        assertThrows(
                AccessDeniedException.class,
                () ->
                        service.validarSuscripcion(
                                authentication,
                                99)
        );
    }

    @Test
    void encargadoValidaScopeDeSede() {

        UsernamePasswordAuthenticationToken authentication =
                auth(
                        "encargado@test.com",
                        "ROLE_ENCARGADO_LOCAL"
                );

        Sede sede =
                new Sede();

        sede.setIdSede(5);

        Conversacion conversacion =
                new Conversacion();

        conversacion.setIdConversacion(30);
        conversacion.setSede(sede);

        when(
                sedeAccessService.esAdministrador(
                        authentication)
        ).thenReturn(false);

        when(
                sedeAccessService.esEncargadoLocal(
                        authentication)
        ).thenReturn(true);

        when(
                conversacionRepository.findById(30)
        ).thenReturn(
                Optional.of(conversacion));

        ChatWebSocketAuthorizationService service =
                service();

        assertDoesNotThrow(() ->
                service.validarSuscripcion(
                        authentication,
                        30)
        );

        verify(
                sedeAccessService
        ).validarAccesoSede(
                authentication,
                5);
    }

    private ChatWebSocketAuthorizationService service() {

        return new ChatWebSocketAuthorizationService(
                clienteRepository,
                conversacionRepository,
                sedeAccessService
        );
    }

    private UsernamePasswordAuthenticationToken auth(
            String correo,
            String authority) {

        return UsernamePasswordAuthenticationToken.authenticated(
                correo,
                null,
                List.of(
                        new SimpleGrantedAuthority(
                                authority)
                )
        );
    }
}