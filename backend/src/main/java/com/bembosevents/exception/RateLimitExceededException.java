package com.bembosevents.exception;

import com.bembosevents.domain.RateLimitScope;

public class RateLimitExceededException
        extends RuntimeException {

    private static final String PUBLIC_MESSAGE =
            "Demasiadas solicitudes. Intenta nuevamente mas tarde";

    private final RateLimitScope scope;

    private final long retryAfterSeconds;

    private final boolean firstRejection;

    public RateLimitExceededException(
            RateLimitScope scope,
            long retryAfterSeconds) {

        this(
                scope,
                retryAfterSeconds,
                true);
    }

    public RateLimitExceededException(
            RateLimitScope scope,
            long retryAfterSeconds,
            boolean firstRejection) {

        super(PUBLIC_MESSAGE);

        if (scope == null) {
            throw new IllegalArgumentException(
                    "scope requerido");
        }

        if (retryAfterSeconds < 1) {
            throw new IllegalArgumentException(
                    "retryAfterSeconds debe ser mayor que cero");
        }

        this.scope =
                scope;

        this.retryAfterSeconds =
                retryAfterSeconds;

        this.firstRejection =
                firstRejection;
    }

    public RateLimitScope getScope() {
        return scope;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }

    public boolean isFirstRejection() {
        return firstRejection;
    }
}
