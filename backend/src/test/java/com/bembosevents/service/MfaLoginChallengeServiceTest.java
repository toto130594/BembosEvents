package com.bembosevents.service;

import com.bembosevents.domain.TipoTokenSeguridad;
import com.bembosevents.entity.Usuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MfaLoginChallengeServiceTest {

    @Mock
    private SecurityTokenLifecycleService
            securityTokenLifecycleService;

    @Test
    void debeEmitirChallengeConTipoMfaYTtlConfigurado() {

        MfaLoginChallengeService service =
                new MfaLoginChallengeService(
                        securityTokenLifecycleService,
                        5);

        Usuario usuario =
                usuario();

        LocalDateTime expiresAt =
                LocalDateTime.of(
                        2026,
                        10,
                        19,
                        18,
                        0);

        when(securityTokenLifecycleService
                .issue(
                        usuario,
                        TipoTokenSeguridad.MFA_LOGIN_CHALLENGE,
                        Duration.ofMinutes(5)))
                .thenReturn(
                        new SecurityTokenLifecycleService.IssuedToken(
                                "challenge-raw",
                                expiresAt));

        MfaLoginChallengeService.IssuedChallenge result =
                service.issue(
                        usuario);

        assertEquals(
                "challenge-raw",
                result.rawToken());

        assertEquals(
                expiresAt,
                result.expiresAt());

        verify(securityTokenLifecycleService)
                .issue(
                        usuario,
                        TipoTokenSeguridad.MFA_LOGIN_CHALLENGE,
                        Duration.ofMinutes(5));
    }

    @Test
    void debeConsumirChallengeConTipoMfa() {

        MfaLoginChallengeService service =
                new MfaLoginChallengeService(
                        securityTokenLifecycleService,
                        5);

        Usuario usuario =
                usuario();

        when(securityTokenLifecycleService
                .consume(
                        "challenge-raw",
                        TipoTokenSeguridad.MFA_LOGIN_CHALLENGE))
                .thenReturn(
                        usuario);

        Usuario result =
                service.consume(
                        "challenge-raw");

        assertEquals(
                usuario,
                result);

        verify(securityTokenLifecycleService)
                .consume(
                        "challenge-raw",
                        TipoTokenSeguridad.MFA_LOGIN_CHALLENGE);
    }

    @Test
    void debeRevocarChallengeActivoDelUsuario() {

        MfaLoginChallengeService service =
                new MfaLoginChallengeService(
                        securityTokenLifecycleService,
                        5);

        Usuario usuario =
                usuario();

        when(securityTokenLifecycleService
                .revokeActive(
                        usuario,
                        TipoTokenSeguridad.MFA_LOGIN_CHALLENGE))
                .thenReturn(
                        1);

        int result =
                service.revokeActive(
                        usuario);

        assertEquals(
                1,
                result);

        verify(securityTokenLifecycleService)
                .revokeActive(
                        usuario,
                        TipoTokenSeguridad.MFA_LOGIN_CHALLENGE);
    }

    @Test
    void debeRechazarTtlCero() {

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        new MfaLoginChallengeService(
                                securityTokenLifecycleService,
                                0));
    }

    @Test
    void debeRechazarTtlNegativo() {

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        new MfaLoginChallengeService(
                                securityTokenLifecycleService,
                                -1));
    }

    private Usuario usuario() {

        Usuario usuario =
                new Usuario();

        usuario.setIdUsuario(
                1);

        usuario.setCorreo(
                "cliente@bembos.test");

        usuario.setActivo(
                true);

        return usuario;
    }
}