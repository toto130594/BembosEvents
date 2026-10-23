package com.bembosevents.controller;

import com.bembosevents.ai.dashboard.AiBackofficeDashboardResponse;
import com.bembosevents.ai.dashboard.AiBackofficeDashboardService;
import com.bembosevents.ai.dashboard.AiDashboardSummary;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiBackofficeDashboardControllerTest {

    @Test
    void delegaConsultaReadOnlyAlServicio() {
        AiBackofficeDashboardService service =
                mock(
                        AiBackofficeDashboardService.class);

        AiBackofficeDashboardController controller =
                new AiBackofficeDashboardController(
                        service);

        Authentication authentication =
                UsernamePasswordAuthenticationToken.authenticated(
                        "admin@test.com",
                        null,
                        List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_ADMINISTRADOR")));

        LocalDate desde =
                LocalDate.of(
                        2026,
                        10,
                        1);

        LocalDate hasta =
                LocalDate.of(
                        2026,
                        10,
                        7);

        AiBackofficeDashboardResponse expected =
                new AiBackofficeDashboardResponse(
                        desde,
                        hasta,
                        null,
                        true,
                        new AiDashboardSummary(
                                0,
                                0,
                                0,
                                0,
                                0,
                                0,
                                0,
                                0),
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of());

        when(service.obtener(
                desde,
                hasta,
                null,
                authentication))
                .thenReturn(
                        expected);

        ResponseEntity<AiBackofficeDashboardResponse> response =
                controller.obtener(
                        desde,
                        hasta,
                        null,
                        authentication);

        assertEquals(
                200,
                response.getStatusCode()
                        .value());

        assertEquals(
                expected,
                response.getBody());

        verify(service)
                .obtener(
                        desde,
                        hasta,
                        null,
                        authentication);
    }
}