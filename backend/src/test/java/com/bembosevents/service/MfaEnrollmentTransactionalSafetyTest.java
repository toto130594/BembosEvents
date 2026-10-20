package com.bembosevents.service;

import com.bembosevents.entity.MfaRecoveryCode;
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
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MfaEnrollmentTransactionalSafetyTest {

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
    void activateDebePermanecerTransactional()
            throws Exception {

        Method activate =
                MfaEnrollmentService.class
                        .getMethod(
                                "activate",
                                String.class,
                                String.class);

        assertTrue(
                activate.isAnnotationPresent(
                        Transactional.class));
    }

    @Test
    void recoveryVacioNoHabilitaMfa() {

        Usuario usuario =
                usuario();

        UsuarioSeguridad seguridad =
                seguridadPendiente(
                        usuario);

        prepararTotpValido(
                usuario,
                seguridad);

        when(mfaRecoveryCodeService
                .generateBatch())
                .thenReturn(
                        List.of());

        assertThrows(
                IllegalStateException.class,
                () ->
                        service.activate(
                                "cliente@bembos.test",
                                "123456"));

        assertFalse(
                seguridad.getMfaHabilitado());

        verify(mfaRecoveryCodeRepository, never())
                .findAllByUsuario_IdUsuarioAndFechaUsoIsNullOrderByIdRecoveryCodeAsc(
                        any());

        verify(mfaRecoveryCodeRepository, never())
                .saveAll(
                        anyList());

        verify(usuarioSeguridadRepository, never())
                .save(
                        seguridad);

        verifyNoInteractions(
                securityAuditService);
    }

    @Test
    void falloGenerandoRecoveryNoHabilitaMfa() {

        Usuario usuario =
                usuario();

        UsuarioSeguridad seguridad =
                seguridadPendiente(
                        usuario);

        prepararTotpValido(
                usuario,
                seguridad);

        when(mfaRecoveryCodeService
                .generateBatch())
                .thenThrow(
                        new IllegalStateException(
                                "fallo recovery"));

        assertThrows(
                IllegalStateException.class,
                () ->
                        service.activate(
                                "cliente@bembos.test",
                                "123456"));

        assertFalse(
                seguridad.getMfaHabilitado());

        verify(mfaRecoveryCodeRepository, never())
                .findAllByUsuario_IdUsuarioAndFechaUsoIsNullOrderByIdRecoveryCodeAsc(
                        any());

        verify(mfaRecoveryCodeRepository, never())
                .saveAll(
                        anyList());

        verify(usuarioSeguridadRepository, never())
                .save(
                        seguridad);

        verifyNoInteractions(
                securityAuditService);
    }

    @Test
    void falloEliminandoRecoveryAnteriorNoHabilitaMfa() {

        Usuario usuario =
                usuario();

        UsuarioSeguridad seguridad =
                seguridadPendiente(
                        usuario);

        prepararTotpValido(
                usuario,
                seguridad);

        when(mfaRecoveryCodeService
                .generateBatch())
                .thenReturn(
                        recoveryMaterials());

        MfaRecoveryCode previous =
                recoveryEntity(
                        usuario,
                        "f".repeat(64));

        List<MfaRecoveryCode> previousUnused =
                List.of(
                        previous);

        when(mfaRecoveryCodeRepository
                .findAllByUsuario_IdUsuarioAndFechaUsoIsNullOrderByIdRecoveryCodeAsc(
                        1))
                .thenReturn(
                        previousUnused);

        doThrow(
                new IllegalStateException(
                        "fallo delete"))
                .when(
                        mfaRecoveryCodeRepository)
                .deleteAll(
                        previousUnused);

        assertThrows(
                IllegalStateException.class,
                () ->
                        service.activate(
                                "cliente@bembos.test",
                                "123456"));

        assertFalse(
                seguridad.getMfaHabilitado());

        verify(mfaRecoveryCodeRepository)
                .deleteAll(
                        previousUnused);

        verify(mfaRecoveryCodeRepository, never())
                .saveAll(
                        anyList());

        verify(usuarioSeguridadRepository, never())
                .save(
                        seguridad);

        verifyNoInteractions(
                securityAuditService);
    }

    @Test
    void falloGuardandoRecoveryNoHabilitaMfa() {

        Usuario usuario =
                usuario();

        UsuarioSeguridad seguridad =
                seguridadPendiente(
                        usuario);

        prepararTotpValido(
                usuario,
                seguridad);

        when(mfaRecoveryCodeService
                .generateBatch())
                .thenReturn(
                        recoveryMaterials());

        when(mfaRecoveryCodeRepository
                .saveAll(
                        anyList()))
                .thenThrow(
                        new IllegalStateException(
                                "fallo save"));

        assertThrows(
                IllegalStateException.class,
                () ->
                        service.activate(
                                "cliente@bembos.test",
                                "123456"));

        assertFalse(
                seguridad.getMfaHabilitado());

        verify(mfaRecoveryCodeRepository)
                .findAllByUsuario_IdUsuarioAndFechaUsoIsNullOrderByIdRecoveryCodeAsc(
                        1);

        verify(mfaRecoveryCodeRepository)
                .saveAll(
                        anyList());

        verify(usuarioSeguridadRepository, never())
                .save(
                        seguridad);

        verifyNoInteractions(
                securityAuditService);
    }

    @Test
    void recoverySePersisteAntesDeHabilitarMfa() {

        Usuario usuario =
                usuario();

        UsuarioSeguridad seguridad =
                seguridadPendiente(
                        usuario);

        prepararTotpValido(
                usuario,
                seguridad);

        List<MfaRecoveryCodeMaterial> materials =
                recoveryMaterials();

        when(mfaRecoveryCodeService
                .generateBatch())
                .thenReturn(
                        materials);

        when(mfaRecoveryCodeRepository
                .saveAll(
                        anyList()))
                .thenAnswer(invocation -> {

                    assertFalse(
                            seguridad.getMfaHabilitado());

                    List<MfaRecoveryCode> entities =
                            invocation.getArgument(0);

                    assertEquals(
                            8,
                            entities.size());

                    for (
                        int i = 0;
                        i < entities.size();
                        i++
                    ) {

                        MfaRecoveryCode entity =
                                entities.get(i);

                        MfaRecoveryCodeMaterial material =
                                materials.get(i);

                        assertEquals(
                                material.codeHash(),
                                entity.getCodigoHash());

                        assertTrue(
                                entity.getCodigoHash()
                                        .matches(
                                                "[0-9a-f]{64}"));
                    }

                    return entities;
                });

        service.activate(
                "cliente@bembos.test",
                "123456");

        assertTrue(
                seguridad.getMfaHabilitado());

        verify(usuarioSeguridadRepository)
                .save(
                        seguridad);
    }

    @Test
    void eliminaSoloRecoveryNoUsadosEncontradosPorRepositorio() {

        Usuario usuario =
                usuario();

        UsuarioSeguridad seguridad =
                seguridadPendiente(
                        usuario);

        prepararTotpValido(
                usuario,
                seguridad);

        when(mfaRecoveryCodeService
                .generateBatch())
                .thenReturn(
                        recoveryMaterials());

        MfaRecoveryCode unused =
                recoveryEntity(
                        usuario,
                        "e".repeat(64));

        List<MfaRecoveryCode> previousUnused =
                List.of(
                        unused);

        when(mfaRecoveryCodeRepository
                .findAllByUsuario_IdUsuarioAndFechaUsoIsNullOrderByIdRecoveryCodeAsc(
                        1))
                .thenReturn(
                        previousUnused);

        service.activate(
                "cliente@bembos.test",
                "123456");

        verify(mfaRecoveryCodeRepository)
                .deleteAll(
                        previousUnused);

        assertTrue(
                seguridad.getMfaHabilitado());
    }

    @Test
    void totpInvalidoNoTocaRecovery() {

        Usuario usuario =
                usuario();

        UsuarioSeguridad seguridad =
                seguridadPendiente(
                        usuario);

        when(usuarioRepository
                .findByCorreoIgnoreCase(
                        "cliente@bembos.test"))
                .thenReturn(
                        Optional.of(usuario));

        when(usuarioSeguridadRepository
                .findByIdUsuarioForUpdate(
                        1))
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

    private void prepararTotpValido(
            Usuario usuario,
            UsuarioSeguridad seguridad) {

        when(usuarioRepository
                .findByCorreoIgnoreCase(
                        "cliente@bembos.test"))
                .thenReturn(
                        Optional.of(usuario));

        when(usuarioSeguridadRepository
                .findByIdUsuarioForUpdate(
                        1))
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

    private MfaRecoveryCode recoveryEntity(
            Usuario usuario,
            String hash) {

        MfaRecoveryCode recoveryCode =
                new MfaRecoveryCode();

        recoveryCode.setUsuario(
                usuario);

        recoveryCode.setCodigoHash(
                hash);

        return recoveryCode;
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

    private UsuarioSeguridad seguridadPendiente(
            Usuario usuario) {

        UsuarioSeguridad seguridad =
                new UsuarioSeguridad();

        seguridad.setUsuario(
                usuario);

        seguridad.setCorreoVerificado(
                true);

        seguridad.setIntentosFallidos(
                0);

        seguridad.setMfaHabilitado(
                false);

        seguridad.setTotpSecretProtegido(
                "v1.secret-protegido");

        return seguridad;
    }
}