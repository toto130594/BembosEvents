package com.bembosevents.security;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ClientRequestIdentityServiceTest {

    private final ClientRequestIdentityService service =
            new ClientRequestIdentityService();

    @Test
    void usaRemoteAddr() {

        HttpServletRequest request =
                mock(
                        HttpServletRequest.class);

        when(request.getRemoteAddr())
                .thenReturn(
                        "192.0.2.10");

        assertEquals(
                "192.0.2.10",
                service.resolve(
                        request));
    }

    @Test
    void ignoraXForwardedForNoConfiable() {

        HttpServletRequest request =
                mock(
                        HttpServletRequest.class);

        when(request.getRemoteAddr())
                .thenReturn(
                        "172.20.0.5");

        when(request.getHeader(
                "X-Forwarded-For"))
                .thenReturn(
                        "203.0.113.100");

        assertEquals(
                "172.20.0.5",
                service.resolve(
                        request));
    }

    @Test
    void requestNuloUsaUnknown() {

        assertEquals(
                "unknown",
                service.resolve(
                        null));
    }

    @Test
    void remoteAddrVacioUsaUnknown() {

        HttpServletRequest request =
                mock(
                        HttpServletRequest.class);

        when(request.getRemoteAddr())
                .thenReturn(
                        " ");

        assertEquals(
                "unknown",
                service.resolve(
                        request));
    }
}
