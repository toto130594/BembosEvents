package com.bembosevents.service;

import com.bembosevents.domain.RateLimitScope;
import com.bembosevents.domain.TipoEventoSeguridad;
import com.bembosevents.exception.RateLimitExceededException;
import com.bembosevents.security.ratelimit.RateLimitPolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RateLimitEnforcementService {

    private final InMemoryRateLimitService
            inMemoryRateLimitService;

    private final SecurityAuditService
            securityAuditService;

    private final boolean enabled;

    private final RateLimitPolicy loginPolicy;

    private final RateLimitPolicy passwordRecoveryPolicy;

    private final RateLimitPolicy emailVerificationPolicy;

    private final RateLimitPolicy mfaChallengePolicy;

    public RateLimitEnforcementService(
            InMemoryRateLimitService inMemoryRateLimitService,
            SecurityAuditService securityAuditService,
            @Value("${app.security.rate-limit.enabled:true}")
            boolean enabled,
            @Value("${app.security.rate-limit.login.max-requests}")
            int loginMaxRequests,
            @Value("${app.security.rate-limit.login.window-seconds}")
            long loginWindowSeconds,
            @Value("${app.security.rate-limit.password-recovery.max-requests}")
            int passwordRecoveryMaxRequests,
            @Value("${app.security.rate-limit.password-recovery.window-seconds}")
            long passwordRecoveryWindowSeconds,
            @Value("${app.security.rate-limit.email-verification.max-requests}")
            int emailVerificationMaxRequests,
                        @Value("${app.security.rate-limit.email-verification.window-seconds}")
            long emailVerificationWindowSeconds,
            @Value("${app.security.rate-limit.mfa-challenge.max-requests}")
            int mfaChallengeMaxRequests,
            @Value("${app.security.rate-limit.mfa-challenge.window-seconds}")
            long mfaChallengeWindowSeconds) {

        this.inMemoryRateLimitService =
                inMemoryRateLimitService;

        this.securityAuditService =
                securityAuditService;

        this.enabled =
                enabled;

        this.loginPolicy =
                policy(
                        loginMaxRequests,
                        loginWindowSeconds);

        this.passwordRecoveryPolicy =
                policy(
                        passwordRecoveryMaxRequests,
                        passwordRecoveryWindowSeconds);

        this.emailVerificationPolicy =
                policy(
                        emailVerificationMaxRequests,
                        emailVerificationWindowSeconds);

        this.mfaChallengePolicy =
                policy(
                        mfaChallengeMaxRequests,
                        mfaChallengeWindowSeconds);
    }

    public void enforce(
            RateLimitScope scope,
            String subject) {

        if (!enabled) {
            return;
        }

        try {
            inMemoryRateLimitService.enforce(
                    scope,
                    subject,
                    policyFor(
                            scope));
        }
        catch (RateLimitExceededException ex) {

            if (ex.isFirstRejection()) {

                securityAuditService.record(
                        null,
                        TipoEventoSeguridad.RATE_LIMIT_EXCEDIDO,
                        false,
                        "Rate limit excedido: "
                                + scope.name());
            }

            throw ex;
        }
    }

    private RateLimitPolicy policyFor(
            RateLimitScope scope) {

        if (scope == null) {
            throw new IllegalArgumentException(
                    "scope requerido");
        }

        return switch (scope) {
            case LOGIN ->
                    loginPolicy;

            case PASSWORD_RECOVERY ->
                    passwordRecoveryPolicy;

            case EMAIL_VERIFICATION ->
                    emailVerificationPolicy;

            case MFA_CHALLENGE ->
                    mfaChallengePolicy;
        };
    }

    private RateLimitPolicy policy(
            int maxRequests,
            long windowSeconds) {

        if (windowSeconds < 1) {
            throw new IllegalArgumentException(
                    "windowSeconds debe ser mayor que cero");
        }

        return new RateLimitPolicy(
                maxRequests,
                Duration.ofSeconds(
                        windowSeconds));
    }
}
