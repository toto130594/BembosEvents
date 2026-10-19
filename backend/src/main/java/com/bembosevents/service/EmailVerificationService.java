package com.bembosevents.service;

import com.bembosevents.domain.TipoEventoSeguridad;
import com.bembosevents.domain.TipoTokenSeguridad;
import com.bembosevents.entity.Usuario;
import com.bembosevents.entity.UsuarioSeguridad;
import com.bembosevents.repository.UsuarioRepository;
import com.bembosevents.repository.UsuarioSeguridadRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Locale;
import java.util.Optional;

@Service
public class EmailVerificationService {

    private final UsuarioRepository usuarioRepository;

    private final UsuarioSeguridadRepository
            usuarioSeguridadRepository;

    private final SecurityTokenLifecycleService
            securityTokenLifecycleService;

    private final SecurityAuditService
            securityAuditService;

    private final ObjectProvider<SecurityEmailService>
            securityEmailServiceProvider;

    private final long tokenTtlHours;

    public EmailVerificationService(
            UsuarioRepository usuarioRepository,
            UsuarioSeguridadRepository usuarioSeguridadRepository,
            SecurityTokenLifecycleService securityTokenLifecycleService,
            SecurityAuditService securityAuditService,
            ObjectProvider<SecurityEmailService>
                    securityEmailServiceProvider,
            @Value("${app.security.email-verification.token-ttl-hours}")
            long tokenTtlHours) {

        if (tokenTtlHours < 1) {
            throw new IllegalArgumentException(
                    "tokenTtlHours debe ser mayor que cero");
        }

        this.usuarioRepository =
                usuarioRepository;

        this.usuarioSeguridadRepository =
                usuarioSeguridadRepository;

        this.securityTokenLifecycleService =
                securityTokenLifecycleService;

        this.securityAuditService =
                securityAuditService;

        this.securityEmailServiceProvider =
                securityEmailServiceProvider;

        this.tokenTtlHours =
                tokenTtlHours;
    }

    @Transactional
    public void solicitarReenvio(
            String correo) {

        String correoNormalizado =
                normalizarCorreo(correo);

        Optional<Usuario> usuarioOptional =
                usuarioRepository
                        .findByCorreoIgnoreCase(
                                correoNormalizado);

        if (usuarioOptional.isEmpty()) {

            securityAuditService.record(
                    null,
                    TipoEventoSeguridad.VERIFICACION_CORREO_REENVIADA,
                    false,
                    "Solicitud procesada sin usuario asociado");

            return;
        }

        Usuario usuario =
                usuarioOptional.get();

        if (!Boolean.TRUE.equals(
                usuario.getActivo())) {

            securityAuditService.record(
                    usuario,
                    TipoEventoSeguridad.VERIFICACION_CORREO_REENVIADA,
                    false,
                    "Solicitud procesada para usuario no elegible");

            return;
        }

        UsuarioSeguridad seguridad =
                usuarioSeguridadRepository
                        .findByIdUsuarioForUpdate(
                                usuario.getIdUsuario())
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Perfil de seguridad no encontrado"));

        if (Boolean.TRUE.equals(
                seguridad.getCorreoVerificado())) {

            securityAuditService.record(
                    usuario,
                    TipoEventoSeguridad.VERIFICACION_CORREO_REENVIADA,
                    false,
                    "Solicitud procesada para correo ya verificado");

            return;
        }

        SecurityEmailService emailService =
                securityEmailServiceProvider
                        .getIfAvailable();

        if (emailService == null) {

            securityAuditService.record(
                    usuario,
                    TipoEventoSeguridad.VERIFICACION_CORREO_REENVIADA,
                    false,
                    "Canal de correo no configurado");

            return;
        }

        SecurityTokenLifecycleService.IssuedToken issuedToken =
                securityTokenLifecycleService.issue(
                        usuario,
                        TipoTokenSeguridad.VERIFICACION_CORREO,
                        Duration.ofHours(
                                tokenTtlHours));

        emailService.sendEmailVerification(
                usuario.getCorreo(),
                issuedToken.rawToken());

        securityAuditService.record(
                usuario,
                TipoEventoSeguridad.VERIFICACION_CORREO_REENVIADA,
                true,
                "Token de verificacion emitido");
    }

    @Transactional
    public void verificar(
            String rawToken) {

        Usuario usuario =
                securityTokenLifecycleService.consume(
                        rawToken,
                        TipoTokenSeguridad.VERIFICACION_CORREO);

        UsuarioSeguridad seguridad =
                usuarioSeguridadRepository
                        .findByIdUsuarioForUpdate(
                                usuario.getIdUsuario())
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Perfil de seguridad no encontrado"));

        seguridad.setCorreoVerificado(
                true);

        usuarioSeguridadRepository.save(
                seguridad);

        securityTokenLifecycleService
                .revokeActive(
                        usuario,
                        TipoTokenSeguridad.VERIFICACION_CORREO);

        securityAuditService.record(
                usuario,
                TipoEventoSeguridad.CORREO_VERIFICADO,
                true,
                "Correo verificado correctamente");
    }

    private String normalizarCorreo(
            String correo) {

        if (correo == null) {
            return "";
        }

        return correo
                .trim()
                .toLowerCase(Locale.ROOT);
    }
}
