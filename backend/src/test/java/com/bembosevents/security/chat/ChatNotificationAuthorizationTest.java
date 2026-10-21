package com.bembosevents.security.chat;

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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatNotificationAuthorizationTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ConversacionRepository conversacionRepository;

    @Mock
    private SedeAccessService sedeAccessService;

    @Test
    void adminPuedeSuscribirseANotificacionesGlobales() {

        UsernamePasswordAuthenticationToken authentication =
                auth(
                        "admin@test.com",
                        "ROLE_ADMINISTRADOR"
                );

        when(
                sedeAccessService
                        .esAdministrador(
                                authentication)
        ).thenReturn(true);

        ChatWebSocketAuthorizationService service =
                service();

        assertDoesNotThrow(() ->
                service.validarSuscripcionNotificacionesGlobal(
                        authentication)
        );
    }

    @Test
    void clienteNoPuedeSuscribirseANotificacionesGlobales() {

        UsernamePasswordAuthenticationToken authentication =
                auth(
                        "cliente@test.com",
                        "ROLE_CLIENTE"
                );

        when(
                sedeAccessService
                        .esAdministrador(
                                authentication)
        ).thenReturn(false);

        ChatWebSocketAuthorizationService service =
                service();

        assertThrows(
                AccessDeniedException.class,
                () ->
                        service.validarSuscripcionNotificacionesGlobal(
                                authentication)
        );
    }

    @Test
    void encargadoUsaScopeDeSedeParaNotificaciones() {

        UsernamePasswordAuthenticationToken authentication =
                auth(
                        "encargado@test.com",
                        "ROLE_ENCARGADO_LOCAL"
                );

        when(
                sedeAccessService
                        .esAdministrador(
                                authentication)
        ).thenReturn(false);

        when(
                sedeAccessService
                        .esEncargadoLocal(
                                authentication)
        ).thenReturn(true);

        ChatWebSocketAuthorizationService service =
                service();

        assertDoesNotThrow(() ->
                service.validarSuscripcionNotificacionesSede(
                        authentication,
                        7)
        );

        verify(
                sedeAccessService
        ).validarAccesoSede(
                authentication,
                7);
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
            String role) {

        return UsernamePasswordAuthenticationToken.authenticated(
                correo,
                null,
                List.of(
                        new SimpleGrantedAuthority(
                                role)
                )
        );
    }
}