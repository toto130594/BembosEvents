package com.bembosevents.security.ratelimit;

public record RateLimitDecision(
        boolean allowed,
        int remaining,
        long retryAfterSeconds,
        boolean firstRejection) {
}
