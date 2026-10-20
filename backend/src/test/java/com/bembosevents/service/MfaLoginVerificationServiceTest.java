package com.bembosevents.service;

import com.bembosevents.domain.TipoEventoSeguridad;
import com.bembosevents.dto.auth.AuthResponse;
import com.bembosevents.entity.MfaRecoveryCode;
import com.bembosevents.entity.Usuario;
import com.bembosevents.entity.UsuarioSeguridad;
import com.bembosevents.repository.MfaRecoveryCodeRepository;
import com.bembosevents.repository.UsuarioSeguridadRepository;
import com.bembosevents.security.mfa.MfaRecoveryCodeService;
import com.bembosevents.security.mfa.MfaSecretProtector;
import com.bembosevents.security.mfa.TotpService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MfaLoginVerificationServiceTest {

    @Mock
    private MfaLoginChallengeService
            mfaLoginChallengeService;

    @Mock
    private UsuarioSeguridadRepository
            usuarioSeguridadRepository;

    @Mock
    private MfaRecoveryCodeRepository
            mfaRecoveryCodeRepository;

    @Mock
    private TotpService totpService;

    @Mock
    private MfaSecretProtector
            mfaSecretProtector;

    @Mock
    private MfaRecoveryCodeService
            mfaRecoveryCodeService;

    @Mock
    private AuthSessionIssuanceService
            authSessionIssuanceService;

    @Mock
    private LoginSecurityService
            loginSecurityService;

    @Mock
    private SecurityAuditService
            securityAuditService;

    @Test
    void totpValidoConsumeChallengeYEmiteAccessYRefresh() {

        MfaLoginVerificationService service =
                service();

        Usuario usuario =
                usuario();

        UsuarioSeguridad seguridad =
                seguridad(usuario);

        prepararChallenge(
                usuario);

        when(usuarioSeguridadRepository
                .findByIdUsuarioForUpdate(1))
                .thenReturn(
                        Optional.of(seguridad));

        when(mfaSecretProtector
                .unprotect(
                        "v1.secret-protegido"))
                .thenReturn(
                        "JBSWY3DPEHPK3PXP");

        when(totpService.verify(
                eq("JBSWY3DPEHPK3PXP"),
                eq("123456"),
                any(Instant.class)))
                .thenReturn(true);

        prepararSesion(
                usuario);

        AuthResponse response =
                service.verifyTotp(
                        "challenge-raw",
                        "123456");

        assertEquals(
                "jwt-final",
                response.token());

        assertEquals(
                "refresh-final",
                response.refreshToken());

        assertEquals(
                2592000000L,
                response.refreshTokenExpiresInMs());

        assertFalse(
                response.mfaRequired());

        verify(mfaLoginChallengeService)
                .consume(
                        "challenge-raw");

        verify(securityAuditService)
                .record(
                        usuario,
                        TipoEventoSeguridad.MFA_VERIFICACION_EXITOSA,
                        true,
                        "Codigo TOTP validado durante login");

        verify(loginSecurityService)
                .recordSuccess(
                        "cliente@bembos.test");
    }

    @Test
    void totpInvalidoAuditaEnRequiresNewSinConsumirChallenge() {

        MfaLoginVerificationService service =
                service();

        Usuario usuario =
                usuario();

        UsuarioSeguridad seguridad =
                seguridad(usuario);

        when(mfaLoginChallengeService
                .resolveActive(
                        "challenge-raw"))
                .thenReturn(usuario);

        when(usuarioSeguridadRepository
                .findByIdUsuarioForUpdate(1))
                .thenReturn(
                        Optional.of(seguridad));

        when(mfaSecretProtector
                .unprotect(
                        "v1.secret-protegido"))
                .thenReturn(
                        "JBSWY3DPEHPK3PXP");

        when(totpService.verify(
                eq("JBSWY3DPEHPK3PXP"),
                eq("000000"),
                any(Instant.class)))
                .thenReturn(false);

        assertThrows(
                BadCredentialsException.class,
                () ->
                        service.verifyTotp(
                                "challenge-raw",
                                "000000"));

        verify(securityAuditService)
                .recordRequiresNew(
                        usuario,
                        TipoEventoSeguridad.MFA_VERIFICACION_FALLIDA,
                        false,
                        "Codigo TOTP invalido durante login");

        verify(mfaLoginChallengeService, never())
                .consume(
                        "challenge-raw");

        verify(authSessionIssuanceService, never())
                .issue(
                        any(Usuario.class));
    }

    @Test
    void recoveryValidoMarcaFechaUsoConsumeChallengeYEmiteAccessYRefresh() {

        MfaLoginVerificationService service =
                service();

        Usuario usuario =
                usuario();

        UsuarioSeguridad seguridad =
                seguridad(usuario);

        prepararChallenge(
                usuario);

        when(usuarioSeguridadRepository
                .findByIdUsuarioForUpdate(1))
                .thenReturn(
                        Optional.of(seguridad));

        MfaRecoveryCode recovery =
                new MfaRecoveryCode();

        recovery.setIdRecoveryCode(
                10L);

        recovery.setUsuario(
                usuario);

        recovery.setCodigoHash(
                "a".repeat(64));

        recovery.setFechaUso(
                null);

        when(mfaRecoveryCodeRepository
                .findAllUnusedForUpdate(1))
                .thenReturn(
                        List.of(recovery));

        when(mfaRecoveryCodeService
                .matches(
                        "AAAA-BBBB-CCCC-DDDD",
                        "a".repeat(64)))
                .thenReturn(true);

        prepararSesion(
                usuario);

        AuthResponse response =
                service.verifyRecovery(
                        "challenge-raw",
                        "AAAA-BBBB-CCCC-DDDD");

        assertEquals(
                "jwt-final",
                response.token());

        assertEquals(
                "refresh-final",
                response.refreshToken());

        assertEquals(
                2592000000L,
                response.refreshTokenExpiresInMs());

        assertNotNull(
                recovery.getFechaUso());

        verify(mfaRecoveryCodeRepository)
                .save(
                        recovery);

        verify(mfaLoginChallengeService)
                .consume(
                        "challenge-raw");

        verify(securityAuditService)
                .record(
                        usuario,
                        TipoEventoSeguridad.MFA_RECOVERY_CODE_USADO,
                        true,
                        "Recovery code MFA usado durante login");

        verify(securityAuditService)
                .record(
                        usuario,
                        TipoEventoSeguridad.MFA_VERIFICACION_EXITOSA,
                        true,
                        "Recovery code validado durante login");

        verify(loginSecurityService)
                .recordSuccess(
                        "cliente@bembos.test");
    }

    @Test
    void recoveryInvalidoNoConsumeNadaYAuditaRequiresNew() {

        MfaLoginVerificationService service =
                service();

        Usuario usuario =
                usuario();

        UsuarioSeguridad seguridad =
                seguridad(usuario);

        when(mfaLoginChallengeService
                .resolveActive(
                        "challenge-raw"))
                .thenReturn(usuario);

        when(usuarioSeguridadRepository
                .findByIdUsuarioForUpdate(1))
                .thenReturn(
                        Optional.of(seguridad));

        MfaRecoveryCode recovery =
                new MfaRecoveryCode();

        recovery.setIdRecoveryCode(
                10L);

        recovery.setUsuario(
                usuario);

        recovery.setCodigoHash(
                "a".repeat(64));

        when(mfaRecoveryCodeRepository
                .findAllUnusedForUpdate(1))
                .thenReturn(
                        List.of(recovery));

        when(mfaRecoveryCodeService
                .matches(
                        "CODIGO-INVALIDO",
                        "a".repeat(64)))
                .thenReturn(false);

        assertThrows(
                BadCredentialsException.class,
                () ->
                        service.verifyRecovery(
                                "challenge-raw",
                                "CODIGO-INVALIDO"));

        verify(securityAuditService)
                .recordRequiresNew(
                        usuario,
                        TipoEventoSeguridad.MFA_VERIFICACION_FALLIDA,
                        false,
                        "Recovery code MFA invalido durante login");

        verify(mfaRecoveryCodeRepository, never())
                .save(
                        any(MfaRecoveryCode.class));

        verify(mfaLoginChallengeService, never())
                .consume(
                        "challenge-raw");

        verify(authSessionIssuanceService, never())
                .issue(
                        any(Usuario.class));
    }

    @Test
    void challengeInvalidoAuditaSinUsuario() {

        MfaLoginVerificationService service =
                service();

        when(mfaLoginChallengeService
                .resolveActive(
                        "challenge-invalido"))
                .thenThrow(
                        new com.bembosevents.exception
                                .TokenSeguridadInvalidoException());

        assertThrows(
                BadCredentialsException.class,
                () ->
                        service.verifyTotp(
                                "challenge-invalido",
                                "123456"));

        verify(securityAuditService)
                .recordRequiresNew(
                        null,
                        TipoEventoSeguridad.MFA_VERIFICACION_FALLIDA,
                        false,
                        "Challenge MFA invalido durante login");
    }

    private MfaLoginVerificationService service() {

        return new MfaLoginVerificationService(
                mfaLoginChallengeService,
                usuarioSeguridadRepository,
                mfaRecoveryCodeRepository,
                totpService,
                mfaSecretProtector,
                mfaRecoveryCodeService,
                authSessionIssuanceService,
                loginSecurityService,
                securityAuditService);
    }

    private void prepararChallenge(
            Usuario usuario) {

        when(mfaLoginChallengeService
                .resolveActive(
                        "challenge-raw"))
                .thenReturn(usuario);

        when(mfaLoginChallengeService
                .consume(
                        "challenge-raw"))
                .thenReturn(usuario);
    }
    private void prepararSesion(
            Usuario usuario) {

        when(authSessionIssuanceService
                .issue(
                        usuario))
                .thenReturn(
                        AuthResponse.authenticated(
                                "jwt-final",
                                "Bearer",
                                3600000L,
                                "refresh-final",
                                2592000000L));
    }
    private Usuario usuario() {

        Usuario usuario =
                new Usuario();

        usuario.setIdUsuario(1);

        usuario.setCorreo(
                "cliente@bembos.test");

        usuario.setActivo(true);

        return usuario;
    }

    private UsuarioSeguridad seguridad(
            Usuario usuario) {

        UsuarioSeguridad seguridad =
                new UsuarioSeguridad();

        seguridad.setUsuario(usuario);
        seguridad.setMfaHabilitado(true);

        seguridad.setTotpSecretProtegido(
                "v1.secret-protegido");

        return seguridad;
    }
}