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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RateLimitEnforcementServiceTest {

    @Mock
    private SecurityAuditService
            securityAuditService;

    @Test
    void primerRechazoAuditaUnaSolaVez() {

        RateLimitEnforcementService service =
                service(
                        true,
                        1);

        service.enforce(
                RateLimitScope.LOGIN,
                "192.0.2.10");

        assertThrows(
                RateLimitExceededException.class,
                () ->
                        service.enforce(
                                RateLimitScope.LOGIN,
                                "192.0.2.10"));

        assertThrows(
                RateLimitExceededException.class,
                () ->
                        service.enforce(
                                RateLimitScope.LOGIN,
                                "192.0.2.10"));

        verify(
                securityAuditService,
                times(1))
                .record(
                        null,
                        TipoEventoSeguridad.RATE_LIMIT_EXCEDIDO,
                        false,
                        "Rate limit excedido: LOGIN");
    }

    @Test
    void deshabilitadoNoLimitaNiAudita() {

        RateLimitEnforcementService service =
                service(
                        false,
                        1);

        for (int i = 0; i < 20; i++) {

            assertDoesNotThrow(
                    () ->
                            service.enforce(
                                    RateLimitScope.LOGIN,
                                    "192.0.2.10"));
        }

        verify(
                securityAuditService,
                never())
                .record(
                        null,
                        TipoEventoSeguridad.RATE_LIMIT_EXCEDIDO,
                        false,
                        "Rate limit excedido: LOGIN");
    }

    @Test
    void scopesTienenContadoresIndependientes() {

        RateLimitEnforcementService service =
                service(
                        true,
                        1);

        service.enforce(
                RateLimitScope.LOGIN,
                "192.0.2.10");

        assertThrows(
                RateLimitExceededException.class,
                () ->
                        service.enforce(
                                RateLimitScope.LOGIN,
                                "192.0.2.10"));

        assertDoesNotThrow(
                () ->
                        service.enforce(
                                RateLimitScope.PASSWORD_RECOVERY,
                                "192.0.2.10"));

        assertDoesNotThrow(
                () ->
                        service.enforce(
                                RateLimitScope.EMAIL_VERIFICATION,
                                "192.0.2.10"));
    }

    private RateLimitEnforcementService service(
            boolean enabled,
            int maxRequests) {

        return new RateLimitEnforcementService(
                new InMemoryRateLimitService(),
                securityAuditService,
                enabled,
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
