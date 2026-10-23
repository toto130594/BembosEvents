package com.bembosevents.ai.dashboard;

import com.bembosevents.entity.AiAnalyticsEvent;
import com.bembosevents.repository.AiAnalyticsEventRepository;
import com.bembosevents.service.SedeAccessService;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiBackofficeDashboardServiceTest {

    @Test
    void administradorPuedeConsultarDashboardGlobal() {
        AiAnalyticsEventRepository repository =
                mock(
                        AiAnalyticsEventRepository.class);

        SedeAccessService access =
                mock(
                        SedeAccessService.class);

        Authentication authentication =
                admin();

        when(access.esAdministrador(
                authentication))
                .thenReturn(
                        true);

        when(repository
                .findAllByFechaEventoGreaterThanEqualAndFechaEventoLessThanOrderByFechaEventoAsc(
                        any(),
                        any()))
                .thenReturn(
                        List.of(
                                event(
                                        "CONSULTAR_DISPONIBILIDAD",
                                        "CONSULTAR_DISPONIBILIDAD",
                                        "TOOL",
                                        true,
                                        100),
                                event(
                                        "CONSULTAR_DISPONIBILIDAD",
                                        "CONSULTAR_DISPONIBILIDAD",
                                        "TOOL",
                                        false,
                                        300),
                                event(
                                        "CHAT_GENERAL",
                                        null,
                                        "PROVIDER",
                                        true,
                                        200)));

        AiBackofficeDashboardResponse response =
                service(
                        repository,
                        access)
                        .obtener(
                                LocalDate.of(
                                        2026,
                                        10,
                                        1),
                                LocalDate.of(
                                        2026,
                                        10,
                                        7),
                                null,
                                authentication);

        assertTrue(
                response.alcanceGlobal());

        assertEquals(
                null,
                response.idSede());

        assertEquals(
                3,
                response.resumen()
                        .totalEventos());

        assertEquals(
                1,
                response.resumen()
                        .errores());

        assertEquals(
                33.33,
                response.resumen()
                        .tasaErrorPorcentaje());

        assertEquals(
                2,
                response.resumen()
                        .eventosTool());

        assertEquals(
                1,
                response.resumen()
                        .eventosProvider());

        assertEquals(
                "CONSULTAR_DISPONIBILIDAD",
                response.intents()
                        .get(0)
                        .clave());

        assertFalse(
                response.insights()
                        .isEmpty());

        verify(repository)
                .findAllByFechaEventoGreaterThanEqualAndFechaEventoLessThanOrderByFechaEventoAsc(
                        any(),
                        any());
    }

    @Test
    void encargadoQuedaLimitadoASedeAsignada() {
        AiAnalyticsEventRepository repository =
                mock(
                        AiAnalyticsEventRepository.class);

        SedeAccessService access =
                mock(
                        SedeAccessService.class);

        Authentication authentication =
                encargado();

        when(access.esAdministrador(
                authentication))
                .thenReturn(
                        false);

        when(access.esEncargadoLocal(
                authentication))
                .thenReturn(
                        true);

        when(access.obtenerSedeAsignada(
                "encargado@test.com"))
                .thenReturn(
                        7);

        when(repository
                .findAllByIdSedeAndFechaEventoGreaterThanEqualAndFechaEventoLessThanOrderByFechaEventoAsc(
                        eq(7),
                        any(),
                        any()))
                .thenReturn(
                        List.of());

        AiBackofficeDashboardResponse response =
                service(
                        repository,
                        access)
                        .obtener(
                                LocalDate.of(
                                        2026,
                                        10,
                                        1),
                                LocalDate.of(
                                        2026,
                                        10,
                                        7),
                                null,
                                authentication);

        assertEquals(
                7,
                response.idSede());

        assertFalse(
                response.alcanceGlobal());

        assertEquals(
                0,
                response.resumen()
                        .totalEventos());

        assertEquals(
                "SIN_DATOS",
                response.insights()
                        .get(0)
                        .tipo());

        verify(repository, never())
                .findAllByFechaEventoGreaterThanEqualAndFechaEventoLessThanOrderByFechaEventoAsc(
                        any(),
                        any());
    }

    @Test
    void encargadoNoPuedeSolicitarOtraSede() {
        AiAnalyticsEventRepository repository =
                mock(
                        AiAnalyticsEventRepository.class);

        SedeAccessService access =
                mock(
                        SedeAccessService.class);

        Authentication authentication =
                encargado();

        when(access.esAdministrador(
                authentication))
                .thenReturn(
                        false);

        when(access.esEncargadoLocal(
                authentication))
                .thenReturn(
                        true);

        when(access.obtenerSedeAsignada(
                "encargado@test.com"))
                .thenReturn(
                        7);

        when(access.puedeAccederSede(
                authentication,
                9))
                .thenReturn(
                        false);

        org.mockito.Mockito
                .doThrow(
                        new AccessDeniedException(
                                "No tiene permisos"))
                .when(
                        access)
                .validarAccesoSede(
                        authentication,
                        9);

        assertThrows(
                AccessDeniedException.class,
                () -> service(
                        repository,
                        access)
                        .obtener(
                                LocalDate.of(
                                        2026,
                                        10,
                                        1),
                                LocalDate.of(
                                        2026,
                                        10,
                                        7),
                                9,
                                authentication));

        verify(repository, never())
                .findAllByIdSedeAndFechaEventoGreaterThanEqualAndFechaEventoLessThanOrderByFechaEventoAsc(
                        any(),
                        any(),
                        any());
    }

    @Test
    void rechazaRangoMayorA31DiasAntesDeConsultarRepositorio() {
        AiAnalyticsEventRepository repository =
                mock(
                        AiAnalyticsEventRepository.class);

        SedeAccessService access =
                mock(
                        SedeAccessService.class);

        assertThrows(
                IllegalArgumentException.class,
                () -> service(
                        repository,
                        access)
                        .obtener(
                                LocalDate.of(
                                        2026,
                                        9,
                                        1),
                                LocalDate.of(
                                        2026,
                                        10,
                                        15),
                                null,
                                admin()));

        verify(repository, never())
                .findAllByFechaEventoGreaterThanEqualAndFechaEventoLessThanOrderByFechaEventoAsc(
                        any(),
                        any());
    }

    private AiBackofficeDashboardService service(
            AiAnalyticsEventRepository repository,
            SedeAccessService access) {

        return new AiBackofficeDashboardService(
                repository,
                access);
    }

    private AiAnalyticsEvent event(
            String intent,
            String tool,
            String route,
            boolean success,
            long duration) {

        AiAnalyticsEvent event =
                new AiAnalyticsEvent();

        event.setIdAiAnalyticsEvent(
                1L);

        event.setIdConversacion(
                10);

        event.setIdSede(
                3);

        event.setIntentType(
                intent);

        event.setToolType(
                tool);

        event.setExecutionRoute(
                route);

        event.setExitoso(
                success);

        event.setDurationMs(
                duration);

        event.setFechaEvento(
                LocalDateTime.of(
                        2026,
                        10,
                        2,
                        10,
                        0));

        return event;
    }

    private Authentication admin() {
        return UsernamePasswordAuthenticationToken.authenticated(
                "admin@test.com",
                null,
                List.of(
                        new SimpleGrantedAuthority(
                                "ROLE_ADMINISTRADOR")));
    }

    private Authentication encargado() {
        return UsernamePasswordAuthenticationToken.authenticated(
                "encargado@test.com",
                null,
                List.of(
                        new SimpleGrantedAuthority(
                                "ROLE_ENCARGADO_LOCAL")));
    }
}