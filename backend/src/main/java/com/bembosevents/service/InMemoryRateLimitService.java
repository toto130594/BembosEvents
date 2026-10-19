package com.bembosevents.service;

import com.bembosevents.domain.RateLimitScope;
import com.bembosevents.exception.RateLimitExceededException;
import com.bembosevents.security.ratelimit.RateLimitDecision;
import com.bembosevents.security.ratelimit.RateLimitPolicy;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.util.HexFormat;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class InMemoryRateLimitService {

    private static final long CLEANUP_INTERVAL =
            256L;

    private final ConcurrentHashMap<String, WindowCounter>
            windows =
            new ConcurrentHashMap<>();

    private final AtomicLong operations =
            new AtomicLong();

    private final Clock clock;

    public InMemoryRateLimitService() {
        this(Clock.systemUTC());
    }

    InMemoryRateLimitService(
            Clock clock) {

        if (clock == null) {
            throw new IllegalArgumentException(
                    "clock requerido");
        }

        this.clock =
                clock;
    }

    public RateLimitDecision evaluate(
            RateLimitScope scope,
            String subject,
            RateLimitPolicy policy) {

        validateArguments(
                scope,
                subject,
                policy);

        long now =
                clock.millis();

        long windowMillis =
                policy.window()
                        .toMillis();

        String key =
                buildKey(
                        scope,
                        subject);

        WindowCounter counter =
                windows.compute(
                        key,
                        (ignored, current) -> {

                            if (current == null
                                    || now >= current.expiresAtMillis()) {

                                return new WindowCounter(
                                        1,
                                        now + windowMillis);
                            }

                            return new WindowCounter(
                                    current.count() + 1,
                                    current.expiresAtMillis());
                        });

        boolean allowed =
                counter.count()
                        <= policy.maxRequests();

        int remaining =
                Math.max(
                        0,
                        policy.maxRequests()
                                - counter.count());

        long retryAfterSeconds =
                allowed
                        ? 0
                        : secondsUntil(
                                now,
                                counter.expiresAtMillis());

        boolean firstRejection =
                !allowed
                        && counter.count()
                        == policy.maxRequests() + 1;

        cleanupIfNecessary(
                now);

        return new RateLimitDecision(
                allowed,
                remaining,
                retryAfterSeconds,
                firstRejection);
    }

    public void enforce(
            RateLimitScope scope,
            String subject,
            RateLimitPolicy policy) {

        RateLimitDecision decision =
                evaluate(
                        scope,
                        subject,
                        policy);

        if (!decision.allowed()) {
            throw new RateLimitExceededException(
                    scope,
                    decision.retryAfterSeconds(),
                    decision.firstRejection());
        }
    }

    int activeBucketCount() {
        return windows.size();
    }

    private void validateArguments(
            RateLimitScope scope,
            String subject,
            RateLimitPolicy policy) {

        if (scope == null) {
            throw new IllegalArgumentException(
                    "scope requerido");
        }

        if (subject == null
                || subject.isBlank()) {

            throw new IllegalArgumentException(
                    "subject requerido");
        }

        if (policy == null) {
            throw new IllegalArgumentException(
                    "policy requerida");
        }
    }

    private String buildKey(
            RateLimitScope scope,
            String subject) {

        return scope.name()
                + ":"
                + sha256(
                        subject.trim());
    }

    private String sha256(
            String value) {

        try {
            MessageDigest digest =
                    MessageDigest.getInstance(
                            "SHA-256");

            byte[] hash =
                    digest.digest(
                            value.getBytes(
                                    StandardCharsets.UTF_8));

            return HexFormat
                    .of()
                    .formatHex(
                            hash);
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(
                    "SHA-256 no disponible",
                    ex);
        }
    }

    private long secondsUntil(
            long now,
            long expiresAtMillis) {

        long remainingMillis =
                Math.max(
                        1,
                        expiresAtMillis - now);

        return Math.max(
                1,
                (remainingMillis + 999L)
                        / 1000L);
    }

    private void cleanupIfNecessary(
            long now) {

        long operation =
                operations.incrementAndGet();

        if (
            operation %
                    CLEANUP_INTERVAL !=
                    0
        ) {
            return;
        }

        windows.entrySet()
                .removeIf(entry ->
                        now >= entry
                                .getValue()
                                .expiresAtMillis());
    }

    private record WindowCounter(
            int count,
            long expiresAtMillis) {
    }
}
