package com.bembosevents.service;

import com.bembosevents.domain.TipoEventoSeguridad;
import com.bembosevents.dto.mfa.MfaActivateResponse;
import com.bembosevents.dto.mfa.MfaSetupResponse;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class MfaEnrollmentService {

    private final UsuarioRepository usuarioRepository;

    private final UsuarioSeguridadRepository
            usuarioSeguridadRepository;

    private final MfaRecoveryCodeRepository
            mfaRecoveryCodeRepository;

    private final TotpService totpService;

    private final MfaSecretProtector
            mfaSecretProtector;

    private final MfaRecoveryCodeService
            mfaRecoveryCodeService;

    private final SecurityAuditService
            securityAuditService;

    public MfaEnrollmentService(
            UsuarioRepository usuarioRepository,
            UsuarioSeguridadRepository usuarioSeguridadRepository,
            MfaRecoveryCodeRepository mfaRecoveryCodeRepository,
            TotpService totpService,
            MfaSecretProtector mfaSecretProtector,
            MfaRecoveryCodeService mfaRecoveryCodeService,
            SecurityAuditService securityAuditService) {

        this.usuarioRepository =
                usuarioRepository;

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

        this.securityAuditService =
                securityAuditService;
    }

    @Transactional
    public MfaSetupResponse setup(
            String correoAutenticado) {

        Usuario usuario =
                getAuthenticatedUser(
                        correoAutenticado);

        UsuarioSeguridad seguridad =
                usuarioSeguridadRepository
                        .findByIdUsuarioForUpdate(
                                usuario.getIdUsuario())
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Perfil de seguridad no inicializado"));

        if (Boolean.TRUE.equals(
                seguridad.getMfaHabilitado())) {

            throw new IllegalStateException(
                    "MFA ya esta habilitado");
        }

        String rawSecret =
                totpService.generateSecret();

        String protectedSecret =
                mfaSecretProtector.protect(
                        rawSecret);

        String otpAuthUri =
                totpService.buildOtpAuthUri(
                        usuario.getCorreo(),
                        rawSecret);

        seguridad.setTotpSecretProtegido(
                protectedSecret);

        seguridad.setMfaHabilitado(
                false);

        usuarioSeguridadRepository.save(
                seguridad);

        securityAuditService.record(
                usuario,
                TipoEventoSeguridad.MFA_SETUP_INICIADO,
                true,
                "Setup MFA iniciado");

        return new MfaSetupResponse(
                rawSecret,
                otpAuthUri,
                false);
    }

    @Transactional
    public MfaActivateResponse activate(
            String correoAutenticado,
            String totpCode) {

        Usuario usuario =
                getAuthenticatedUser(
                        correoAutenticado);

        UsuarioSeguridad seguridad =
                usuarioSeguridadRepository
                        .findByIdUsuarioForUpdate(
                                usuario.getIdUsuario())
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Perfil de seguridad no inicializado"));

        if (Boolean.TRUE.equals(
                seguridad.getMfaHabilitado())) {

            throw new IllegalStateException(
                    "MFA ya esta habilitado");
        }

        String protectedSecret =
                seguridad.getTotpSecretProtegido();

        if (protectedSecret == null
                || protectedSecret.isBlank()) {

            throw new IllegalStateException(
                    "No existe un setup MFA pendiente");
        }

        /*
         * El secreto TOTP solamente existe en texto plano
         * temporalmente en memoria.
         */
        String rawSecret =
                mfaSecretProtector.unprotect(
                        protectedSecret);

        boolean valid =
                totpService.verify(
                        rawSecret,
                        totpCode,
                        Instant.now());

        if (!valid) {

            /*
             * No se modifica MFA ni recovery codes.
             *
             * El metodo completo es transaccional, por lo que
             * ningun estado parcial debe persistirse.
             */
            throw new IllegalArgumentException(
                    "Codigo TOTP invalido");
        }

        List<MfaRecoveryCodeMaterial> materials =
                mfaRecoveryCodeService
                        .generateBatch();

        /*
         * Defensa adicional:
         * MFA nunca puede quedar habilitado sin al menos
         * un recovery code valido.
         */
        if (materials == null
                || materials.isEmpty()) {

            throw new IllegalStateException(
                    "No se pudieron generar recovery codes");
        }

        /*
         * Una nueva activacion invalida cualquier material
         * recovery anterior que pudiera existir.
         */
        List<MfaRecoveryCode> previousUnusedRecoveryCodes =
                mfaRecoveryCodeRepository
                        .findAllByUsuario_IdUsuarioAndFechaUsoIsNullOrderByIdRecoveryCodeAsc(
                                usuario.getIdUsuario());

        /*
         * Solo los recovery codes NO USADOS de una configuracion
         * anterior se invalidan.
         *
         * Los codigos ya usados permanecen como historial.
         */
        if (!previousUnusedRecoveryCodes.isEmpty()) {

            mfaRecoveryCodeRepository.deleteAll(
                    previousUnusedRecoveryCodes);
        }

        List<MfaRecoveryCode> recoveryEntities =
                new ArrayList<>();

        List<String> rawRecoveryCodes =
                new ArrayList<>();

        for (
            MfaRecoveryCodeMaterial material
                : materials
        ) {

            MfaRecoveryCode recoveryCode =
                    new MfaRecoveryCode();

            recoveryCode.setUsuario(
                    usuario);

            /*
             * Solo SHA-256 entra a persistencia.
             * Nunca se guarda rawCode.
             */
            recoveryCode.setCodigoHash(
                    material.codeHash());

            recoveryEntities.add(
                    recoveryCode);

            /*
             * Los codigos raw solamente forman parte de
             * la respuesta de activacion de una sola vez.
             */
            rawRecoveryCodes.add(
                    material.rawCode());
        }

        mfaRecoveryCodeRepository.saveAll(
                recoveryEntities);

        /*
         * MFA se habilita solamente DESPUES de generar
         * y persistir los recovery hashes.
         */
        seguridad.setMfaHabilitado(
                true);

        usuarioSeguridadRepository.save(
                seguridad);

        securityAuditService.record(
                usuario,
                TipoEventoSeguridad.MFA_VERIFICACION_EXITOSA,
                true,
                "Codigo TOTP validado durante activacion");

        securityAuditService.record(
                usuario,
                TipoEventoSeguridad.MFA_ACTIVADO,
                true,
                "MFA activado correctamente");

        return new MfaActivateResponse(
                true,
                rawRecoveryCodes);
    }

    private Usuario getAuthenticatedUser(
            String correoAutenticado) {

        if (correoAutenticado == null
                || correoAutenticado.isBlank()) {

            throw new IllegalArgumentException(
                    "Usuario autenticado requerido");
        }

        String correo =
                correoAutenticado
                        .trim()
                        .toLowerCase(
                                Locale.ROOT);

        Usuario usuario =
                usuarioRepository
                        .findByCorreoIgnoreCase(
                                correo)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Usuario autenticado no disponible"));

        if (!Boolean.TRUE.equals(
                usuario.getActivo())) {

            throw new IllegalStateException(
                    "Usuario autenticado no disponible");
        }

        return usuario;
    }
}