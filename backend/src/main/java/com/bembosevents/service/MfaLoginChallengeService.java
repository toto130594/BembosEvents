package com.bembosevents.service;

import com.bembosevents.domain.TipoTokenSeguridad;
import com.bembosevents.entity.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class MfaLoginChallengeService {

    private final SecurityTokenLifecycleService
            securityTokenLifecycleService;

    private final Duration challengeTtl;

    public MfaLoginChallengeService(
            SecurityTokenLifecycleService
                    securityTokenLifecycleService,
            @Value("${app.security.mfa.challenge-ttl-minutes:5}")
            long challengeTtlMinutes) {

        if (challengeTtlMinutes < 1) {
            throw new IllegalArgumentException(
                    "challengeTtlMinutes debe ser mayor que cero");
        }

        this.securityTokenLifecycleService =
                securityTokenLifecycleService;

        this.challengeTtl =
                Duration.ofMinutes(
                        challengeTtlMinutes);
    }

    public IssuedChallenge issue(
            Usuario usuario) {

        SecurityTokenLifecycleService.IssuedToken issued =
                securityTokenLifecycleService.issue(
                        usuario,
                        TipoTokenSeguridad.MFA_LOGIN_CHALLENGE,
                        challengeTtl);

        return new IssuedChallenge(
                issued.rawToken(),
                issued.expiresAt());
    }

    public Usuario resolveActive(
            String rawChallenge) {

        return securityTokenLifecycleService.resolveActive(
                rawChallenge,
                TipoTokenSeguridad.MFA_LOGIN_CHALLENGE);
    }
    public Usuario consume(
            String rawChallenge) {

        return securityTokenLifecycleService.consume(
                rawChallenge,
                TipoTokenSeguridad.MFA_LOGIN_CHALLENGE);
    }

    public int revokeActive(
            Usuario usuario) {

        return securityTokenLifecycleService.revokeActive(
                usuario,
                TipoTokenSeguridad.MFA_LOGIN_CHALLENGE);
    }

    public long getChallengeTtlMs() {
        return challengeTtl.toMillis();
    }

    public record IssuedChallenge(
            String rawToken,
            LocalDateTime expiresAt) {
    }
}