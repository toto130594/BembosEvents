package com.bembosevents.security;

import com.bembosevents.domain.RateLimitScope;
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

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RateLimitMfaFilterTest {

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
    void protegeTotpConScopeMfaChallenge()
            throws Exception {

        MockHttpServletRequest request =
                request(
                        "/api/auth/mfa/verificar");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        filter.doFilter(
                request,
                response,
                filterChain);

        verify(rateLimitEnforcementService)
                .enforce(
                        RateLimitScope.MFA_CHALLENGE,
                        "192.0.2.10");
    }

    @Test
    void protegeRecoveryConMismoScopeMfaChallenge()
            throws Exception {

        MockHttpServletRequest request =
                request(
                        "/api/auth/mfa/recuperacion");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        filter.doFilter(
                request,
                response,
                filterChain);

        verify(rateLimitEnforcementService)
                .enforce(
                        RateLimitScope.MFA_CHALLENGE,
                        "192.0.2.10");
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