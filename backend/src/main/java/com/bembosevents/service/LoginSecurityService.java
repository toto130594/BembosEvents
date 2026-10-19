package com.bembosevents.service;

import com.bembosevents.domain.TipoEventoSeguridad;
import com.bembosevents.entity.Usuario;
import com.bembosevents.entity.UsuarioSeguridad;
import com.bembosevents.repository.UsuarioRepository;
import com.bembosevents.repository.UsuarioSeguridadRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@Service
public class LoginSecurityService {

    private static final String PUBLIC_CREDENTIALS_MESSAGE =
            "Credenciales invalidas";

    private final UsuarioRepository usuarioRepository;

    private final UsuarioSeguridadRepository
            usuarioSeguridadRepository;

    private final SecurityAuditService securityAuditService;

    private final int maxFailedAttempts;

    private final long lockDurationMinutes;

    public LoginSecurityService(
            UsuarioRepository usuarioRepository,
            UsuarioSeguridadRepository usuarioSeguridadRepository,
            SecurityAuditService securityAuditService,
            @Value("${app.security.login.max-failed-attempts}")
            int maxFailedAttempts,
            @Value("${app.security.login.lock-duration-minutes}")
            long lockDurationMinutes) {

        if (maxFailedAttempts < 1) {
            throw new IllegalArgumentException(
                    "maxFailedAttempts debe ser mayor que cero");
        }

        if (lockDurationMinutes < 1) {
            throw new IllegalArgumentException(
                    "lockDurationMinutes debe ser mayor que cero");
        }

        this.usuarioRepository =
                usuarioRepository;

        this.usuarioSeguridadRepository =
                usuarioSeguridadRepository;

        this.securityAuditService =
                securityAuditService;

        this.maxFailedAttempts =
                maxFailedAttempts;

        this.lockDurationMinutes =
                lockDurationMinutes;
    }

    @Transactional
    public void initializeForCurrentRegistration(
            Usuario usuario) {

        if (usuario == null
                || usuario.getIdUsuario() == null) {

            throw new IllegalArgumentException(
                    "Usuario persistido requerido");
        }

        if (usuarioSeguridadRepository
                .existsById(usuario.getIdUsuario())) {
            return;
        }

        createSecurityProfile(
                usuario,
                true);
    }

    @Transactional
    public void validateLoginAllowed(
            String correo) {

        Optional<Usuario> usuarioOptional =
                usuarioRepository
                        .findByCorreoIgnoreCase(
                                correo);

        if (usuarioOptional.isEmpty()) {
            return;
        }

        Usuario usuario =
                usuarioOptional.get();

        UsuarioSeguridad seguridad =
                getOrCreateForUpdate(
                        usuario);

        LocalDateTime bloqueadoHasta =
                seguridad.getBloqueadoHasta();

        if (bloqueadoHasta == null) {
            return;
        }

        LocalDateTime now =
                utcNow();

        if (bloqueadoHasta.isAfter(now)) {
            throw new BadCredentialsException(
                    PUBLIC_CREDENTIALS_MESSAGE);
        }

        seguridad.setIntentosFallidos(0);
        seguridad.setBloqueadoHasta(null);

        usuarioSeguridadRepository.save(
                seguridad);

        securityAuditService.record(
                usuario,
                TipoEventoSeguridad.USUARIO_DESBLOQUEADO,
                true,
                "Bloqueo temporal expirado");
    }

    @Transactional
    public void recordFailure(
            String correo) {

        Optional<Usuario> usuarioOptional =
                usuarioRepository
                        .findByCorreoIgnoreCase(
                                correo);

        if (usuarioOptional.isEmpty()) {

            securityAuditService.record(
                    null,
                    TipoEventoSeguridad.LOGIN_FALLIDO,
                    false,
                    "Credenciales invalidas");

            return;
        }

        Usuario usuario =
                usuarioOptional.get();

        UsuarioSeguridad seguridad =
                getOrCreateForUpdate(
                        usuario);

        int currentAttempts =
                seguridad.getIntentosFallidos() == null
                        ? 0
                        : seguridad.getIntentosFallidos();

        int nextAttempts =
                Math.min(
                        currentAttempts + 1,
                        maxFailedAttempts);

        seguridad.setIntentosFallidos(
                nextAttempts);

        boolean mustBlock =
                nextAttempts >=
                        maxFailedAttempts;

        if (mustBlock) {
            seguridad.setBloqueadoHasta(
                    utcNow().plusMinutes(
                            lockDurationMinutes));
        }

        usuarioSeguridadRepository.save(
                seguridad);

        securityAuditService.record(
                usuario,
                TipoEventoSeguridad.LOGIN_FALLIDO,
                false,
                "Credenciales invalidas");

        if (mustBlock) {
            securityAuditService.record(
                    usuario,
                    TipoEventoSeguridad.USUARIO_BLOQUEADO,
                    true,
                    "Bloqueo temporal aplicado");
        }
    }

    @Transactional
    public void recordRejected(
            String correo) {

        Usuario usuario =
                usuarioRepository
                        .findByCorreoIgnoreCase(
                                correo)
                        .orElse(null);

        securityAuditService.record(
                usuario,
                TipoEventoSeguridad.LOGIN_FALLIDO,
                false,
                "Credenciales invalidas");
    }

    @Transactional
    public void recordSuccess(
            String correo) {

        Optional<Usuario> usuarioOptional =
                usuarioRepository
                        .findByCorreoIgnoreCase(
                                correo);

        if (usuarioOptional.isEmpty()) {
            return;
        }

        Usuario usuario =
                usuarioOptional.get();

        UsuarioSeguridad seguridad =
                getOrCreateForUpdate(
                        usuario);

        seguridad.setIntentosFallidos(0);
        seguridad.setBloqueadoHasta(null);

        usuarioSeguridadRepository.save(
                seguridad);

        securityAuditService.record(
                usuario,
                TipoEventoSeguridad.LOGIN_EXITOSO,
                true,
                "Login correcto");
    }

    private UsuarioSeguridad getOrCreateForUpdate(
            Usuario usuario) {

        return usuarioSeguridadRepository
                .findByIdUsuarioForUpdate(
                        usuario.getIdUsuario())
                .orElseGet(() ->
                        createSecurityProfile(
                                usuario,
                                true));
    }

    private UsuarioSeguridad createSecurityProfile(
            Usuario usuario,
            boolean correoVerificado) {

        UsuarioSeguridad seguridad =
                new UsuarioSeguridad();

        seguridad.setUsuario(usuario);
        seguridad.setCorreoVerificado(
                correoVerificado);
        seguridad.setIntentosFallidos(0);
        seguridad.setBloqueadoHasta(null);
        seguridad.setMfaHabilitado(false);
        seguridad.setTotpSecretProtegido(null);

        return usuarioSeguridadRepository.save(
                seguridad);
    }

    private LocalDateTime utcNow() {
        return LocalDateTime.now(
                ZoneOffset.UTC);
    }
}
