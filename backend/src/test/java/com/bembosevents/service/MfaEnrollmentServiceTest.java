package com.bembosevents.service;

import com.bembosevents.domain.TipoEventoSeguridad;
import com.bembosevents.dto.mfa.MfaActivateResponse;
import com.bembosevents.dto.mfa.MfaSetupResponse;
import com.bembosevents.entity.Usuario;
import com.bembosevents.entity.UsuarioSeguridad;
import com.bembosevents.repository.MfaRecoveryCodeRepository;
import com.bembosevents.repository.UsuarioRepository;
import com.bembosevents.repository.UsuarioSeguridadRepository;
import com.bembosevents.security.mfa.MfaRecoveryCodeMaterial;
import com.bembosevents.security.mfa.MfaRecoveryCodeService;
import com.bembosevents.security.mfa.MfaSecretProtector;
import com.bembosevents.security.mfa.TotpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MfaEnrollmentServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

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
    private SecurityAuditService
            securityAuditService;

    private MfaEnrollmentService service;

    @BeforeEach
    void setUp() {

        service =
                new MfaEnrollmentService(
                        usuarioRepository,
                        usuarioSeguridadRepository,
                        mfaRecoveryCodeRepository,
                        totpService,
                        mfaSecretProtector,
                        mfaRecoveryCodeService,
                        securityAuditService);
    }

    @Test
    void setupGeneraProtegeYPersisteSecreto() {

        Usuario usuario =
                usuario();

        UsuarioSeguridad seguridad =
                seguridad(
                        usuario,
                        false);

        when(usuarioRepository
                .findByCorreoIgnoreCase(
                        "cliente@bembos.test"))
                .thenReturn(
                        Optional.of(usuario));

        when(usuarioSeguridadRepository
                .findByIdUsuarioForUpdate(1))
                .thenReturn(
                        Optional.of(seguridad));

        when(totpService.generateSecret())
                .thenReturn(
                        "JBSWY3DPEHPK3PXP");

        when(mfaSecretProtector
                .protect(
                        "JBSWY3DPEHPK3PXP"))
                .thenReturn(
                        "v1.valor-protegido");

        when(totpService
                .buildOtpAuthUri(
                        "cliente@bembos.test",
                        "JBSWY3DPEHPK3PXP"))
                .thenReturn(
                        "otpauth://totp/Bembos%20Events:cliente");

        MfaSetupResponse response =
                service.setup(
                        "CLIENTE@BEMBOS.TEST");

        assertEquals(
                "JBSWY3DPEHPK3PXP",
                response.secret());

        assertEquals(
                "otpauth://totp/Bembos%20Events:cliente",
                response.otpauthUri());

        assertFalse(
                response.mfaHabilitado());

        assertFalse(
                seguridad.getMfaHabilitado());

        assertEquals(
                "v1.valor-protegido",
                seguridad.getTotpSecretProtegido());

        verify(usuarioSeguridadRepository)
                .save(
                        seguridad);

        verify(securityAuditService)
                .record(
                        usuario,
                        TipoEventoSeguridad.MFA_SETUP_INICIADO,
                        true,
                        "Setup MFA iniciado");
    }

    @Test
    void setupNuevoReemplazaSetupPendiente() {

        Usuario usuario =
                usuario();

        UsuarioSeguridad seguridad =
                seguridad(
                        usuario,
                        false);

        seguridad.setTotpSecretProtegido(
                "v1.setup-anterior");

        when(usuarioRepository
                .findByCorreoIgnoreCase(
                        "cliente@bembos.test"))
                .thenReturn(
                        Optional.of(usuario));

        when(usuarioSeguridadRepository
                .findByIdUsuarioForUpdate(1))
                .thenReturn(
                        Optional.of(seguridad));

        when(totpService.generateSecret())
                .thenReturn(
                        "NUEVOSECRETOBASE32");

        when(mfaSecretProtector
                .protect(
                        "NUEVOSECRETOBASE32"))
                .thenReturn(
                        "v1.setup-nuevo");

        when(totpService
                .buildOtpAuthUri(
                        "cliente@bembos.test",
                        "NUEVOSECRETOBASE32"))
                .thenReturn(
                        "otpauth://totp/nuevo");

        service.setup(
                "cliente@bembos.test");

        assertEquals(
                "v1.setup-nuevo",
                seguridad.getTotpSecretProtegido());

        assertFalse(
                seguridad.getMfaHabilitado());
    }

    @Test
    void setupRechazaUsuarioConMfaActivo() {

        Usuario usuario =
                usuario();

        UsuarioSeguridad seguridad =
                seguridad(
                        usuario,
                        true);

        when(usuarioRepository
                .findByCorreoIgnoreCase(
                        "cliente@bembos.test"))
                .thenReturn(
                        Optional.of(usuario));

        when(usuarioSeguridadRepository
                .findByIdUsuarioForUpdate(1))
                .thenReturn(
                        Optional.of(seguridad));

        assertThrows(
                IllegalStateException.class,
                () ->
                        service.setup(
                                "cliente@bembos.test"));

        verifyNoInteractions(
                totpService);

        verifyNoInteractions(
                mfaSecretProtector);

        verify(usuarioSeguridadRepository, never())
                .save(
                        seguridad);
    }

    @Test
    void activateValidaTotpGeneraRecoveryYHabilitaMfa() {

        Usuario usuario =
                usuario();

        UsuarioSeguridad seguridad =
                seguridad(
                        usuario,
                        false);

        seguridad.setTotpSecretProtegido(
                "v1.secreto-protegido");

        when(usuarioRepository
                .findByCorreoIgnoreCase(
                        "cliente@bembos.test"))
                .thenReturn(
                        Optional.of(usuario));

        when(usuarioSeguridadRepository
                .findByIdUsuarioForUpdate(1))
                .thenReturn(
                        Optional.of(seguridad));

        when(mfaSecretProtector
                .unprotect(
                        "v1.secreto-protegido"))
                .thenReturn(
                        "JBSWY3DPEHPK3PXP");

        when(totpService.verify(
                eq("JBSWY3DPEHPK3PXP"),
                eq("123456"),
                any(Instant.class)))
                .thenReturn(true);

        List<MfaRecoveryCodeMaterial> materials =
                recoveryMaterials();

        when(mfaRecoveryCodeService
                .generateBatch())
                .thenReturn(
                        materials);

        MfaActivateResponse response =
                service.activate(
                        "cliente@bembos.test",
                        "123456");

        assertTrue(
                response.mfaHabilitado());

        assertEquals(
                8,
                response.recoveryCodes()
                        .size());

        assertTrue(
                seguridad.getMfaHabilitado());

        verify(mfaRecoveryCodeRepository)
                .findAllByUsuario_IdUsuarioAndFechaUsoIsNullOrderByIdRecoveryCodeAsc(
                        1);

        verify(mfaRecoveryCodeRepository)
                .saveAll(
                        anyList());

        verify(usuarioSeguridadRepository)
                .save(
                        seguridad);

        verify(securityAuditService)
                .record(
                        usuario,
                        TipoEventoSeguridad.MFA_VERIFICACION_EXITOSA,
                        true,
                        "Codigo TOTP validado durante activacion");

        verify(securityAuditService)
                .record(
                        usuario,
                        TipoEventoSeguridad.MFA_ACTIVADO,
                        true,
                        "MFA activado correctamente");
    }

    @Test
    void activateConTotpInvalidoNoHabilitaMfa() {

        Usuario usuario =
                usuario();

        UsuarioSeguridad seguridad =
                seguridad(
                        usuario,
                        false);

        seguridad.setTotpSecretProtegido(
                "v1.secreto-protegido");

        when(usuarioRepository
                .findByCorreoIgnoreCase(
                        "cliente@bembos.test"))
                .thenReturn(
                        Optional.of(usuario));

        when(usuarioSeguridadRepository
                .findByIdUsuarioForUpdate(1))
                .thenReturn(
                        Optional.of(seguridad));

        when(mfaSecretProtector
                .unprotect(
                        "v1.secreto-protegido"))
                .thenReturn(
                        "JBSWY3DPEHPK3PXP");

        when(totpService.verify(
                eq("JBSWY3DPEHPK3PXP"),
                eq("000000"),
                any(Instant.class)))
                .thenReturn(false);

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.activate(
                                "cliente@bembos.test",
                                "000000"));

        assertFalse(
                seguridad.getMfaHabilitado());

        verifyNoInteractions(
                mfaRecoveryCodeService);

        verify(mfaRecoveryCodeRepository, never())
                .findAllByUsuario_IdUsuarioAndFechaUsoIsNullOrderByIdRecoveryCodeAsc(
                        any());

        verify(mfaRecoveryCodeRepository, never())
                .saveAll(
                        anyList());

        verify(usuarioSeguridadRepository, never())
                .save(
                        seguridad);
    }

    @Test
    void activateSinSetupPendienteEsRechazado() {

        Usuario usuario =
                usuario();

        UsuarioSeguridad seguridad =
                seguridad(
                        usuario,
                        false);

        seguridad.setTotpSecretProtegido(
                null);

        when(usuarioRepository
                .findByCorreoIgnoreCase(
                        "cliente@bembos.test"))
                .thenReturn(
                        Optional.of(usuario));

        when(usuarioSeguridadRepository
                .findByIdUsuarioForUpdate(1))
                .thenReturn(
                        Optional.of(seguridad));

        assertThrows(
                IllegalStateException.class,
                () ->
                        service.activate(
                                "cliente@bembos.test",
                                "123456"));

        verifyNoInteractions(
                mfaSecretProtector);

        verifyNoInteractions(
                mfaRecoveryCodeService);

        verify(mfaRecoveryCodeRepository, never())
                .saveAll(
                        anyList());
    }

    @Test
    void activateRechazaMfaYaActivo() {

        Usuario usuario =
                usuario();

        UsuarioSeguridad seguridad =
                seguridad(
                        usuario,
                        true);

        seguridad.setTotpSecretProtegido(
                "v1.secreto-protegido");

        when(usuarioRepository
                .findByCorreoIgnoreCase(
                        "cliente@bembos.test"))
                .thenReturn(
                        Optional.of(usuario));

        when(usuarioSeguridadRepository
                .findByIdUsuarioForUpdate(1))
                .thenReturn(
                        Optional.of(seguridad));

        assertThrows(
                IllegalStateException.class,
                () ->
                        service.activate(
                                "cliente@bembos.test",
                                "123456"));

        verifyNoInteractions(
                mfaSecretProtector);

        verifyNoInteractions(
                mfaRecoveryCodeService);

        verify(mfaRecoveryCodeRepository, never())
                .saveAll(
                        anyList());
    }

    private List<MfaRecoveryCodeMaterial>
            recoveryMaterials() {

        List<MfaRecoveryCodeMaterial> result =
                new ArrayList<>();

        for (int i = 0; i < 8; i++) {

            String codeHash =
                    String.format(
                            "%064x",
                            i + 1);

            result.add(
                    new MfaRecoveryCodeMaterial(
                            "AAAA-BBBB-CCCC-" + i,
                            codeHash));
        }

        return result;
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
            Usuario usuario,
            boolean mfaHabilitado) {

        UsuarioSeguridad seguridad =
                new UsuarioSeguridad();

        seguridad.setUsuario(
                usuario);

        seguridad.setCorreoVerificado(
                true);

        seguridad.setIntentosFallidos(
                0);

        seguridad.setMfaHabilitado(
                mfaHabilitado);

        return seguridad;
    }
}