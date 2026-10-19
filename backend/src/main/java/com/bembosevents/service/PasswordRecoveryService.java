package com.bembosevents.service;

import com.bembosevents.domain.TipoEventoSeguridad;
import com.bembosevents.domain.TipoTokenSeguridad;
import com.bembosevents.entity.Usuario;
import com.bembosevents.entity.UsuarioSeguridad;
import com.bembosevents.repository.SesionUsuarioRepository;
import com.bembosevents.repository.UsuarioRepository;
import com.bembosevents.repository.UsuarioSeguridadRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.Optional;

@Service
public class PasswordRecoveryService {

    private final UsuarioRepository usuarioRepository;

    private final UsuarioSeguridadRepository
            usuarioSeguridadRepository;

    private final SesionUsuarioRepository
            sesionUsuarioRepository;

    private final SecurityTokenLifecycleService
            securityTokenLifecycleService;

    private final SecurityAuditService
            securityAuditService;

    private final ObjectProvider<SecurityEmailService>
            securityEmailServiceProvider;

    private final PasswordEncoder passwordEncoder;

    private final long tokenTtlMinutes;

    public PasswordRecoveryService(
            UsuarioRepository usuarioRepository,
            UsuarioSeguridadRepository usuarioSeguridadRepository,
            SesionUsuarioRepository sesionUsuarioRepository,
            SecurityTokenLifecycleService securityTokenLifecycleService,
            SecurityAuditService securityAuditService,
            ObjectProvider<SecurityEmailService>
                    securityEmailServiceProvider,
            PasswordEncoder passwordEncoder,
            @Value("${app.security.password-recovery.token-ttl-minutes}")
            long tokenTtlMinutes) {

        if (tokenTtlMinutes < 1) {
            throw new IllegalArgumentException(
                    "tokenTtlMinutes debe ser mayor que cero");
        }

        this.usuarioRepository =
                usuarioRepository;

        this.usuarioSeguridadRepository =
                usuarioSeguridadRepository;

        this.sesionUsuarioRepository =
                sesionUsuarioRepository;

        this.securityTokenLifecycleService =
                securityTokenLifecycleService;

        this.securityAuditService =
                securityAuditService;

        this.securityEmailServiceProvider =
                securityEmailServiceProvider;

        this.passwordEncoder =
                passwordEncoder;

        this.tokenTtlMinutes =
                tokenTtlMinutes;
    }

    @Transactional
    public void solicitar(
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
                    TipoEventoSeguridad.PASSWORD_RESET_SOLICITADO,
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
                    TipoEventoSeguridad.PASSWORD_RESET_SOLICITADO,
                    false,
                    "Solicitud procesada para usuario no elegible");

            return;
        }

        SecurityEmailService emailService =
                securityEmailServiceProvider
                        .getIfAvailable();

        if (emailService == null) {

            securityAuditService.record(
                    usuario,
                    TipoEventoSeguridad.PASSWORD_RESET_SOLICITADO,
                    false,
                    "Canal de correo no configurado");

            return;
        }

        SecurityTokenLifecycleService.IssuedToken issuedToken =
                securityTokenLifecycleService.issue(
                        usuario,
                        TipoTokenSeguridad.RECUPERACION_PASSWORD,
                        Duration.ofMinutes(
                                tokenTtlMinutes));

        emailService.sendPasswordRecovery(
                usuario.getCorreo(),
                issuedToken.rawToken());

        securityAuditService.record(
                usuario,
                TipoEventoSeguridad.PASSWORD_RESET_SOLICITADO,
                true,
                "Solicitud de recuperacion emitida");
    }

    @Transactional
    public void restablecer(
            String rawToken,
            String nuevaPassword) {

        if (nuevaPassword == null
                || nuevaPassword.isBlank()) {

            throw new IllegalArgumentException(
                    "La contrasena es obligatoria");
        }

        Usuario usuario =
                securityTokenLifecycleService.consume(
                        rawToken,
                        TipoTokenSeguridad.RECUPERACION_PASSWORD);

        LocalDateTime now =
                utcNow();

        usuario.setPasswordHash(
                passwordEncoder.encode(
                        nuevaPassword));

        usuario.setFechaActualizacion(
                now);

        usuarioRepository.save(
                usuario);

        sesionUsuarioRepository
                .revokeActiveSessions(
                        usuario.getIdUsuario(),
                        now);

        securityTokenLifecycleService
                .revokeActive(
                        usuario,
                        TipoTokenSeguridad.RECUPERACION_PASSWORD);

        UsuarioSeguridad seguridad =
                usuarioSeguridadRepository
                        .findByIdUsuarioForUpdate(
                                usuario.getIdUsuario())
                        .orElse(null);

        if (seguridad != null) {

            seguridad.setIntentosFallidos(0);
            seguridad.setBloqueadoHasta(null);

            usuarioSeguridadRepository.save(
                    seguridad);
        }

        securityAuditService.record(
                usuario,
                TipoEventoSeguridad.PASSWORD_RESET_COMPLETADO,
                true,
                "Password restablecido correctamente");
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

    private LocalDateTime utcNow() {
        return LocalDateTime.now(
                ZoneOffset.UTC);
    }
}
