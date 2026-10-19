package com.bembosevents.service;

import com.bembosevents.domain.RateLimitScope;
import com.bembosevents.exception.RateLimitExceededException;
import com.bembosevents.security.ratelimit.RateLimitDecision;
import com.bembosevents.security.ratelimit.RateLimitPolicy;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryRateLimitServiceTest {

    @Test
    void permiteSolicitudesHastaElLimite() {

        MutableClock clock =
                new MutableClock(
                        Instant.parse(
                                "2026-10-19T12:00:00Z"));

        InMemoryRateLimitService service =
                new InMemoryRateLimitService(
                        clock);

        RateLimitPolicy policy =
                new RateLimitPolicy(
                        3,
                        Duration.ofSeconds(60));

        RateLimitDecision first =
                service.evaluate(
                        RateLimitScope.LOGIN,
                        "192.0.2.10",
                        policy);

        RateLimitDecision second =
                service.evaluate(
                        RateLimitScope.LOGIN,
                        "192.0.2.10",
                        policy);

        RateLimitDecision third =
                service.evaluate(
                        RateLimitScope.LOGIN,
                        "192.0.2.10",
                        policy);

        assertTrue(
                first.allowed());

        assertEquals(
                2,
                first.remaining());

        assertTrue(
                second.allowed());

        assertEquals(
                1,
                second.remaining());

        assertTrue(
                third.allowed());

        assertEquals(
                0,
                third.remaining());
    }

    @Test
    void rechazaCuandoSuperaLimite() {

        MutableClock clock =
                new MutableClock(
                        Instant.parse(
                                "2026-10-19T12:00:00Z"));

        InMemoryRateLimitService service =
                new InMemoryRateLimitService(
                        clock);

        RateLimitPolicy policy =
                new RateLimitPolicy(
                        2,
                        Duration.ofSeconds(60));

        service.enforce(
                RateLimitScope.LOGIN,
                "192.0.2.10",
                policy);

        service.enforce(
                RateLimitScope.LOGIN,
                "192.0.2.10",
                policy);

        RateLimitExceededException ex =
                assertThrows(
                        RateLimitExceededException.class,
                        () ->
                                service.enforce(
                                        RateLimitScope.LOGIN,
                                        "192.0.2.10",
                                        policy));

        assertEquals(
                RateLimitScope.LOGIN,
                ex.getScope());

        assertTrue(
                ex.getRetryAfterSeconds()
                        >= 1);

        assertEquals(
                "Demasiadas solicitudes. Intenta nuevamente mas tarde",
                ex.getMessage());
    }

    @Test
    void nuevaVentanaReiniciaContador() {

        MutableClock clock =
                new MutableClock(
                        Instant.parse(
                                "2026-10-19T12:00:00Z"));

        InMemoryRateLimitService service =
                new InMemoryRateLimitService(
                        clock);

        RateLimitPolicy policy =
                new RateLimitPolicy(
                        1,
                        Duration.ofSeconds(30));

        assertTrue(
                service.evaluate(
                        RateLimitScope.LOGIN,
                        "192.0.2.10",
                        policy)
                        .allowed());

        assertFalse(
                service.evaluate(
                        RateLimitScope.LOGIN,
                        "192.0.2.10",
                        policy)
                        .allowed());

        clock.advance(
                Duration.ofSeconds(31));

        RateLimitDecision newWindow =
                service.evaluate(
                        RateLimitScope.LOGIN,
                        "192.0.2.10",
                        policy);

        assertTrue(
                newWindow.allowed());

        assertEquals(
                0,
                newWindow.remaining());
    }

    @Test
    void subjectsDistintosSonIndependientes() {

        MutableClock clock =
                new MutableClock(
                        Instant.parse(
                                "2026-10-19T12:00:00Z"));

        InMemoryRateLimitService service =
                new InMemoryRateLimitService(
                        clock);

        RateLimitPolicy policy =
                new RateLimitPolicy(
                        1,
                        Duration.ofMinutes(1));

        service.enforce(
                RateLimitScope.LOGIN,
                "192.0.2.10",
                policy);

        assertThrows(
                RateLimitExceededException.class,
                () ->
                        service.enforce(
                                RateLimitScope.LOGIN,
                                "192.0.2.10",
                                policy));

        assertDoesNotThrow(
                () ->
                        service.enforce(
                                RateLimitScope.LOGIN,
                                "192.0.2.11",
                                policy));
    }

    @Test
    void scopesDistintosSonIndependientes() {

        MutableClock clock =
                new MutableClock(
                        Instant.parse(
                                "2026-10-19T12:00:00Z"));

        InMemoryRateLimitService service =
                new InMemoryRateLimitService(
                        clock);

        RateLimitPolicy policy =
                new RateLimitPolicy(
                        1,
                        Duration.ofMinutes(1));

        service.enforce(
                RateLimitScope.LOGIN,
                "192.0.2.10",
                policy);

        assertThrows(
                RateLimitExceededException.class,
                () ->
                        service.enforce(
                                RateLimitScope.LOGIN,
                                "192.0.2.10",
                                policy));

        assertDoesNotThrow(
                () ->
                        service.enforce(
                                RateLimitScope.PASSWORD_RECOVERY,
                                "192.0.2.10",
                                policy));
    }

    @Test
    void policyInvalidaEsRechazada() {

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        new RateLimitPolicy(
                                0,
                                Duration.ofMinutes(1)));

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        new RateLimitPolicy(
                                1,
                                Duration.ZERO));
    }

    private static final class MutableClock
            extends Clock {

        private Instant instant;

        private final ZoneId zone;

        private MutableClock(
                Instant instant) {

            this(
                    instant,
                    ZoneOffset.UTC);
        }

        private MutableClock(
                Instant instant,
                ZoneId zone) {

            this.instant =
                    instant;

            this.zone =
                    zone;
        }

        private void advance(
                Duration duration) {

            instant =
                    instant.plus(
                            duration);
        }

        @Override
        public ZoneId getZone() {
            return zone;
        }

        @Override
        public Clock withZone(
                ZoneId newZone) {

            return new MutableClock(
                    instant,
                    newZone);
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
