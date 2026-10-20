package com.bembosevents.service;

import com.bembosevents.domain.RateLimitScope;
import com.bembosevents.domain.TipoEventoSeguridad;
import com.bembosevents.exception.RateLimitExceededException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RateLimitMfaScopeTest {

    @Mock
    private SecurityAuditService
            securityAuditService;

    @Test
    void mfaTieneContadorIndependienteDeLogin() {

        RateLimitEnforcementService service =
                service(
                        1);

        service.enforce(
                RateLimitScope.MFA_CHALLENGE,
                "192.0.2.10");

        assertThrows(
                RateLimitExceededException.class,
                () ->
                        service.enforce(
                                RateLimitScope.MFA_CHALLENGE,
                                "192.0.2.10"));

        assertDoesNotThrow(
                () ->
                        service.enforce(
                                RateLimitScope.LOGIN,
                                "192.0.2.10"));

        verify(securityAuditService)
                .record(
                        null,
                        TipoEventoSeguridad.RATE_LIMIT_EXCEDIDO,
                        false,
                        "Rate limit excedido: MFA_CHALLENGE");
    }

    private RateLimitEnforcementService service(
            int maxRequests) {

        return new RateLimitEnforcementService(
                new InMemoryRateLimitService(),
                securityAuditService,
                true,
                maxRequests,
                60,
                maxRequests,
                60,
                maxRequests,
                60,
                maxRequests,
                60);
    }
}