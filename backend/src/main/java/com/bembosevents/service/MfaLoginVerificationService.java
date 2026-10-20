package com.bembosevents.service;

import com.bembosevents.domain.TipoEventoSeguridad;
import com.bembosevents.dto.auth.AuthResponse;
import com.bembosevents.entity.MfaRecoveryCode;
import com.bembosevents.entity.Usuario;
import com.bembosevents.entity.UsuarioSeguridad;
import com.bembosevents.exception.TokenSeguridadInvalidoException;
import com.bembosevents.repository.MfaRecoveryCodeRepository;
import com.bembosevents.repository.UsuarioSeguridadRepository;
import com.bembosevents.security.mfa.MfaRecoveryCodeService;
import com.bembosevents.security.mfa.MfaSecretProtector;
import com.bembosevents.security.mfa.TotpService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class MfaLoginVerificationService {

    private static final String PUBLIC_CREDENTIALS_MESSAGE =
            "Credenciales invalidas";

    private final MfaLoginChallengeService
            mfaLoginChallengeService;

    private final UsuarioSeguridadRepository
            usuarioSeguridadRepository;

    private final MfaRecoveryCodeRepository
            mfaRecoveryCodeRepository;

    private final TotpService totpService;

    private final MfaSecretProtector
            mfaSecretProtector;

    private final MfaRecoveryCodeService
            mfaRecoveryCodeService;
    private final AuthSessionIssuanceService
            authSessionIssuanceService;

    private final LoginSecurityService
            loginSecurityService;

    private final SecurityAuditService
            securityAuditService;

    public MfaLoginVerificationService(
            MfaLoginChallengeService
                    mfaLoginChallengeService,
            UsuarioSeguridadRepository
                    usuarioSeguridadRepository,
            MfaRecoveryCodeRepository
                    mfaRecoveryCodeRepository,
            TotpService totpService,
            MfaSecretProtector mfaSecretProtector,
            MfaRecoveryCodeService
                    mfaRecoveryCodeService,
            AuthSessionIssuanceService
                    authSessionIssuanceService,
            LoginSecurityService loginSecurityService,
            SecurityAuditService securityAuditService) {

        this.mfaLoginChallengeService =
                mfaLoginChallengeService;

        this.usuarioSeguridadRepository =
                usuarioSeguridadRepository;

        this.mfaRecoveryCodeRepository =
                mfaRecoveryCodeRepository;

        this.totpService =
                totpService;

        this.mfaSecretProtector =
                mfaSecretProtector;

        this.mfaRecoveryCodeService =
                mfaRecoveryCodeService;

        this.authSessionIssuanceService =
                authSessionIssuanceService;

        this.loginSecurityService =
                loginSecurityService;

        this.securityAuditService =
                securityAuditService;
    }

    @Transactional
    public AuthResponse verifyTotp(
            String rawChallenge,
            String code) {

        Usuario usuario =
                resolveChallenge(
                        rawChallenge);

        UsuarioSeguridad seguridad =
                requireEnabledMfa(
                        usuario);

        String protectedSecret =
                seguridad.getTotpSecretProtegido();

        if (protectedSecret == null
                || protectedSecret.isBlank()) {

            auditFailure(
                    usuario,
                    "Estado MFA invalido durante login");

            throw invalidCredentials();
        }

        String rawSecret =
                mfaSecretProtector.unprotect(
                        protectedSecret);

        boolean valido =
                totpService.verify(
                        rawSecret,
                        code,
                        Instant.now());

        if (!valido) {

            auditFailure(
                    usuario,
                    "Codigo TOTP invalido durante login");

            throw invalidCredentials();
        }

        return completeLogin(
                usuario,
                rawChallenge,
                false);
    }

    @Transactional
    public AuthResponse verifyRecovery(
            String rawChallenge,
            String rawRecoveryCode) {

        Usuario usuario =
                resolveChallenge(
                        rawChallenge);

        requireEnabledMfa(
                usuario);

        List<MfaRecoveryCode> candidates =
                mfaRecoveryCodeRepository
                        .findAllUnusedForUpdate(
                                usuario.getIdUsuario());

        MfaRecoveryCode matched =
                null;

        for (MfaRecoveryCode candidate : candidates) {

            if (recoveryMatches(
                    rawRecoveryCode,
                    candidate.getCodigoHash())) {

                if (matched == null) {
                    matched =
                            candidate;
                }
            }
        }

        if (matched == null) {

            auditFailure(
                    usuario,
                    "Recovery code MFA invalido durante login");

            throw invalidCredentials();
        }

        matched.setFechaUso(
                LocalDateTime.now(
                        ZoneOffset.UTC));

        mfaRecoveryCodeRepository.save(
                matched);

        return completeLogin(
                usuario,
                rawChallenge,
                true);
    }

    private UsuarioSeguridad requireEnabledMfa(
            Usuario usuario) {

        if (!Boolean.TRUE.equals(
                usuario.getActivo())) {

            auditFailure(
                    usuario,
                    "Usuario invalido durante MFA login");

            throw invalidCredentials();
        }

        UsuarioSeguridad seguridad =
                usuarioSeguridadRepository
                        .findByIdUsuarioForUpdate(
                                usuario.getIdUsuario())
                        .orElse(null);

        if (seguridad == null
                || !Boolean.TRUE.equals(
                        seguridad.getMfaHabilitado())) {

            auditFailure(
                    usuario,
                    "Estado MFA invalido durante login");

            throw invalidCredentials();
        }

        return seguridad;
    }

    private boolean recoveryMatches(
            String rawRecoveryCode,
            String expectedHash) {

        try {
            return mfaRecoveryCodeService.matches(
                    rawRecoveryCode,
                    expectedHash);
        }
        catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private AuthResponse completeLogin(
            Usuario usuario,
            String rawChallenge,
            boolean recoveryUsed) {

        Usuario consumedUsuario =
                consumeChallenge(
                        rawChallenge);

        if (consumedUsuario.getIdUsuario() == null
                || !consumedUsuario.getIdUsuario()
                        .equals(
                                usuario.getIdUsuario())) {

            throw new IllegalStateException(
                    "Challenge MFA inconsistente");
        }

        AuthResponse response =
                authSessionIssuanceService
                        .issue(
                                usuario);

        if (recoveryUsed) {

            securityAuditService.record(
                    usuario,
                    TipoEventoSeguridad.MFA_RECOVERY_CODE_USADO,
                    true,
                    "Recovery code MFA usado durante login");
        }

        securityAuditService.record(
                usuario,
                TipoEventoSeguridad.MFA_VERIFICACION_EXITOSA,
                true,
                recoveryUsed
                        ? "Recovery code validado durante login"
                        : "Codigo TOTP validado durante login");

        loginSecurityService.recordSuccess(
                usuario.getCorreo());

        return response;
    }

    private Usuario resolveChallenge(
            String rawChallenge) {

        try {
            return mfaLoginChallengeService
                    .resolveActive(
                            rawChallenge);
        }
        catch (TokenSeguridadInvalidoException ex) {

            securityAuditService.recordRequiresNew(
                    null,
                    TipoEventoSeguridad.MFA_VERIFICACION_FALLIDA,
                    false,
                    "Challenge MFA invalido durante login");

            throw invalidCredentials();
        }
    }

    private Usuario consumeChallenge(
            String rawChallenge) {

        try {
            return mfaLoginChallengeService
                    .consume(
                            rawChallenge);
        }
        catch (TokenSeguridadInvalidoException ex) {
            throw invalidCredentials();
        }
    }

    private void auditFailure(
            Usuario usuario,
            String detalleSeguro) {

        securityAuditService.recordRequiresNew(
                usuario,
                TipoEventoSeguridad.MFA_VERIFICACION_FALLIDA,
                false,
                detalleSeguro);
    }

    private BadCredentialsException
    invalidCredentials() {

        return new BadCredentialsException(
                PUBLIC_CREDENTIALS_MESSAGE);
    }
}