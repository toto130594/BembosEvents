package com.bembosevents.service;

import com.bembosevents.dto.auth.SesionResponse;

import com.bembosevents.domain.TipoEventoSeguridad;
import com.bembosevents.entity.SesionUsuario;
import com.bembosevents.entity.RefreshTokenHistory;
import com.bembosevents.exception.RecursoNoEncontradoException;
import com.bembosevents.entity.Usuario;
import com.bembosevents.repository.SesionUsuarioRepository;
import com.bembosevents.repository.RefreshTokenHistoryRepository;
import com.bembosevents.security.SecurityTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class SesionUsuarioService {

    private static final int USER_AGENT_MAX_LENGTH = 500;
    private static final int IP_MAX_LENGTH = 64;

    private final SesionUsuarioRepository sesionUsuarioRepository;
    private final SecurityTokenService securityTokenService;
    private final SecurityAuditService securityAuditService;

    private final RefreshTokenHistoryRepository

            refreshTokenHistoryRepository;


    private final RefreshTokenReuseService

            refreshTokenReuseService;


    private final long refreshTtlDays;

    public SesionUsuarioService(
            SesionUsuarioRepository sesionUsuarioRepository,
            SecurityTokenService securityTokenService,
            SecurityAuditService securityAuditService,
            RefreshTokenHistoryRepository refreshTokenHistoryRepository,
            RefreshTokenReuseService refreshTokenReuseService,
            @Value("${app.security.session.refresh-ttl-days}")
            long refreshTtlDays) {

        if (refreshTtlDays < 1) {
            throw new IllegalArgumentException(
                    "refreshTtlDays debe ser mayor que cero");
        }

        this.sesionUsuarioRepository =
                sesionUsuarioRepository;

        this.securityTokenService =
                securityTokenService;

        this.securityAuditService =
                securityAuditService;

        this.refreshTokenHistoryRepository =

                refreshTokenHistoryRepository;


        this.refreshTokenReuseService =

                refreshTokenReuseService;


        this.refreshTtlDays =
                refreshTtlDays;
    }

    @Transactional
    public IssuedRefreshToken crear(
            Usuario usuario,
            String userAgent,
            String ipOrigen) {

        validarUsuario(usuario);

        LocalDateTime now =
                utcNow();

        String rawRefreshToken =
                securityTokenService
                        .generateOpaqueToken();

        SesionUsuario sesion =
                new SesionUsuario();

        sesion.setUsuario(usuario);

        sesion.setRefreshTokenHash(
                securityTokenService
                        .hashToken(
                                rawRefreshToken));

        sesion.setFechaCreacion(now);

        sesion.setFechaExpiracion(
                now.plusDays(
                        refreshTtlDays));

        sesion.setFechaUltimoUso(now);
        sesion.setFechaRevocacion(null);

        sesion.setUserAgent(
                normalizeMetadata(
                        userAgent,
                        USER_AGENT_MAX_LENGTH));

        sesion.setIpOrigen(
                normalizeMetadata(
                        ipOrigen,
                        IP_MAX_LENGTH));

        sesionUsuarioRepository.save(
                sesion);

        securityAuditService.record(
                usuario,
                TipoEventoSeguridad.SESION_CREADA,
                true,
                "Sesion persistente creada");

        return new IssuedRefreshToken(
                rawRefreshToken,
                sesion.getFechaExpiracion(),
                usuario);
    }

    @Transactional(noRollbackFor = BadCredentialsException.class)
    public IssuedRefreshToken rotar(
            String rawRefreshToken) {

        if (rawRefreshToken == null
                || rawRefreshToken.isBlank()) {

            throw invalidRefreshToken();
        }

        String currentHash =
                securityTokenService
                        .hashToken(
                                rawRefreshToken);

        SesionUsuario sesion =
                sesionUsuarioRepository
                        .findByRefreshTokenHashForUpdate(
                                currentHash)
                        .orElse(null);

        if (sesion == null) {

            /*
             * Si el hash ya no es vigente, comprobar si fue
             * utilizado anteriormente.
             *
             * La deteccion de reuse participa en esta transaccion para
             * persistir revocacion y auditoria aunque
             * esta operacion devuelva el 401 generico.
             */
            refreshTokenReuseService
                    .detectAndRevokeIfReused(
                            currentHash);

            throw invalidRefreshToken();
        }

        LocalDateTime now =
                utcNow();

        if (sesion.getFechaRevocacion() != null
                || sesion.getFechaExpiracion() == null
                || !sesion.getFechaExpiracion()
                        .isAfter(now)
                || !securityTokenService.matches(
                        rawRefreshToken,
                        sesion.getRefreshTokenHash())) {

            throw invalidRefreshToken();
        }

        RefreshTokenHistory history =

                new RefreshTokenHistory();


        history.setSesionUsuario(

                sesion);


        history.setRefreshTokenHash(

                currentHash);


        history.setFechaRotacion(

                now);


        history.setFechaExpiracion(

                sesion.getFechaExpiracion());


        refreshTokenHistoryRepository.save(

                history);


        String rotatedRawToken =
                securityTokenService
                        .generateOpaqueToken();

        sesion.setRefreshTokenHash(
                securityTokenService
                        .hashToken(
                                rotatedRawToken));

        sesion.setFechaUltimoUso(
                now);

        sesionUsuarioRepository.save(
                sesion);

        securityAuditService.record(
                sesion.getUsuario(),
                TipoEventoSeguridad.SESION_RENOVADA,
                true,
                "Sesion persistente renovada");

        return new IssuedRefreshToken(
                rotatedRawToken,
                sesion.getFechaExpiracion(),
                sesion.getUsuario());
    }

    private void validarUsuario(
            Usuario usuario) {

        if (usuario == null
                || usuario.getIdUsuario() == null) {

            throw new IllegalArgumentException(
                    "Usuario persistido requerido");
        }
    }

    private String normalizeMetadata(
            String value,
            int maxLength) {

        if (value == null) {
            return null;
        }

        String normalized =
                value.trim();

        if (normalized.isBlank()) {
            return null;
        }

        if (normalized.length()
                <= maxLength) {

            return normalized;
        }

        return normalized.substring(
                0,
                maxLength);
    }
    @Transactional
    public void revocar(
            String rawRefreshToken) {

        if (rawRefreshToken == null
                || rawRefreshToken.isBlank()) {

            throw invalidRefreshToken();
        }

        String refreshTokenHash =
                securityTokenService
                        .hashToken(
                                rawRefreshToken);

        SesionUsuario sesion =
                sesionUsuarioRepository
                        .findByRefreshTokenHashForUpdate(
                                refreshTokenHash)
                        .orElseThrow(
                                this::invalidRefreshToken);

        LocalDateTime now =
                utcNow();

        if (sesion.getFechaRevocacion() != null) {
            throw invalidRefreshToken();
        }

        if (sesion.getFechaExpiracion() == null
                || !sesion.getFechaExpiracion()
                        .isAfter(now)) {

            throw invalidRefreshToken();
        }

        if (!securityTokenService.matches(
                rawRefreshToken,
                sesion.getRefreshTokenHash())) {

            throw invalidRefreshToken();
        }

        sesion.setFechaRevocacion(
                now);

        sesionUsuarioRepository.save(
                sesion);

        securityAuditService.record(
                sesion.getUsuario(),
                TipoEventoSeguridad.SESION_REVOCADA,
                true,
                "Sesion persistente revocada");
    }

    @Transactional
    public int revocarTodas(
            Usuario usuario) {

        validarUsuario(
                usuario);

        LocalDateTime now =
                utcNow();

        int sesionesRevocadas =
                sesionUsuarioRepository
                        .revokeActiveSessions(
                                usuario.getIdUsuario(),
                                now);

        securityAuditService.record(
                usuario,
                TipoEventoSeguridad.TODAS_SESIONES_REVOCADAS,
                true,
                "Sesiones persistentes revocadas: "
                        + sesionesRevocadas);

        return sesionesRevocadas;
    }

    @Transactional(readOnly = true)
    public List<SesionResponse> listarActivas(
            Usuario usuario) {

        validarUsuario(
                usuario);

        LocalDateTime now =
                utcNow();

        return sesionUsuarioRepository
                .findAllByUsuario_IdUsuarioAndFechaRevocacionIsNullAndFechaExpiracionAfterOrderByFechaCreacionDesc(
                        usuario.getIdUsuario(),
                        now)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void revocarPropia(
            Usuario usuario,
            Long idSesionUsuario) {

        validarUsuario(
                usuario);

        if (idSesionUsuario == null
                || idSesionUsuario <= 0) {

            throw new IllegalArgumentException(
                    "El id de sesion debe ser positivo");
        }

        LocalDateTime now =
                utcNow();

        SesionUsuario sesion =
                sesionUsuarioRepository
                        .findActiveOwnedByIdForUpdate(
                                idSesionUsuario,
                                usuario.getIdUsuario(),
                                now)
                        .orElseThrow(
                                () ->
                                        new RecursoNoEncontradoException(
                                                "Sesion no encontrada"));

        sesion.setFechaRevocacion(
                now);

        sesionUsuarioRepository.save(
                sesion);

        securityAuditService.record(
                usuario,
                TipoEventoSeguridad.SESION_REVOCADA,
                true,
                "Sesion persistente revocada");
    }

    private SesionResponse toResponse(
            SesionUsuario sesion) {

        return new SesionResponse(
                sesion.getIdSesionUsuario(),
                sesion.getFechaCreacion(),
                sesion.getFechaExpiracion(),
                sesion.getFechaUltimoUso(),
                sesion.getUserAgent(),
                sesion.getIpOrigen());
    }
    private BadCredentialsException
            invalidRefreshToken() {

        return new BadCredentialsException(
                "Refresh token invalido");
    }

    private LocalDateTime utcNow() {
        return LocalDateTime.now(
                ZoneOffset.UTC);
    }

    public record IssuedRefreshToken(
            String rawRefreshToken,
            LocalDateTime expiresAt,
            Usuario usuario) {

        public IssuedRefreshToken(
                String rawRefreshToken,
                LocalDateTime expiresAt) {

            this(
                    rawRefreshToken,
                    expiresAt,
                    null);
        }
    }
}