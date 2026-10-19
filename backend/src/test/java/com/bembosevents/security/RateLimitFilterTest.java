package com.bembosevents.security;

import com.bembosevents.domain.RateLimitScope;
import com.bembosevents.exception.RateLimitExceededException;
import com.bembosevents.service.RateLimitEnforcementService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RateLimitFilterTest {

    @Mock
    private RateLimitEnforcementService
            rateLimitEnforcementService;

    @Mock
    private FilterChain filterChain;

    private RateLimitFilter filter;

    @BeforeEach
    void setUp() {

        ObjectMapper objectMapper =
                new ObjectMapper()
                        .findAndRegisterModules();

        filter =
                new RateLimitFilter(
                        rateLimitEnforcementService,
                        new ClientRequestIdentityService(),
                        objectMapper);
    }

    @Test
    void protegeLogin() throws Exception {

        MockHttpServletRequest request =
                request(
                        "/api/auth/login");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        filter.doFilter(
                request,
                response,
                filterChain);

        verify(rateLimitEnforcementService)
                .enforce(
                        RateLimitScope.LOGIN,
                        "192.0.2.10");

        verify(filterChain)
                .doFilter(
                        request,
                        response);
    }

    @Test
    void protegePasswordRecovery() throws Exception {

        MockHttpServletRequest request =
                request(
                        "/api/auth/password/solicitar");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        filter.doFilter(
                request,
                response,
                filterChain);

        verify(rateLimitEnforcementService)
                .enforce(
                        RateLimitScope.PASSWORD_RECOVERY,
                        "192.0.2.10");
    }

    @Test
    void protegeReenvioVerificacion() throws Exception {

        MockHttpServletRequest request =
                request(
                        "/api/auth/correo/reenviar");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        filter.doFilter(
                request,
                response,
                filterChain);

        verify(rateLimitEnforcementService)
                .enforce(
                        RateLimitScope.EMAIL_VERIFICATION,
                        "192.0.2.10");
    }

    @Test
    void noProtegeRegistro() throws Exception {

        MockHttpServletRequest request =
                request(
                        "/api/auth/registro");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        filter.doFilter(
                request,
                response,
                filterChain);

        verify(
                rateLimitEnforcementService,
                never())
                .enforce(
                        RateLimitScope.LOGIN,
                        "192.0.2.10");

        verify(filterChain)
                .doFilter(
                        request,
                        response);
    }

    @Test
    void limiteExcedidoRetorna429YRetryAfter()
            throws Exception {

        MockHttpServletRequest request =
                request(
                        "/api/auth/login");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        doThrow(
                new RateLimitExceededException(
                        RateLimitScope.LOGIN,
                        42,
                        true))
                .when(
                        rateLimitEnforcementService)
                .enforce(
                        RateLimitScope.LOGIN,
                        "192.0.2.10");

        filter.doFilter(
                request,
                response,
                filterChain);

        assertEquals(
                429,
                response.getStatus());

        assertEquals(
                "42",
                response.getHeader(
                        "Retry-After"));

        assertTrue(
                response.getContentAsString()
                        .contains(
                                "Demasiadas solicitudes"));

        verify(
                filterChain,
                never())
                .doFilter(
                        request,
                        response);
    }

    private MockHttpServletRequest request(
            String uri) {

        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "POST",
                        uri);

        request.setRemoteAddr(
                "192.0.2.10");

        return request;
    }
}
