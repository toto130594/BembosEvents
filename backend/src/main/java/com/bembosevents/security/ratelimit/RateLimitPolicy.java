package com.bembosevents.security.ratelimit;

import java.time.Duration;

public record RateLimitPolicy(
        int maxRequests,
        Duration window) {

    public RateLimitPolicy {

        if (maxRequests < 1) {
            throw new IllegalArgumentException(
                    "maxRequests debe ser mayor que cero");
        }

        if (window == null
                || window.isZero()
                || window.isNegative()) {

            throw new IllegalArgumentException(
                    "window debe ser mayor que cero");
        }
    }
}
