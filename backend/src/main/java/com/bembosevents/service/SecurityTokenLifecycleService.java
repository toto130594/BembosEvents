package com.bembosevents.service;

import com.bembosevents.domain.TipoTokenSeguridad;
import com.bembosevents.entity.TokenSeguridadUsuario;
import com.bembosevents.entity.Usuario;
import com.bembosevents.exception.TokenSeguridadInvalidoException;
import com.bembosevents.repository.TokenSeguridadUsuarioRepository;
import com.bembosevents.security.SecurityTokenService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class SecurityTokenLifecycleService {

    private final TokenSeguridadUsuarioRepository
            tokenSeguridadUsuarioRepository;

    private final SecurityTokenService securityTokenService;

    public SecurityTokenLifecycleService(
            TokenSeguridadUsuarioRepository
                    tokenSeguridadUsuarioRepository,
            SecurityTokenService securityTokenService) {

        this.tokenSeguridadUsuarioRepository =
                tokenSeguridadUsuarioRepository;

        this.securityTokenService =
                securityTokenService;
    }

    @Transactional
    public IssuedToken issue(
            Usuario usuario,
            TipoTokenSeguridad tipo,
            Duration ttl) {

        validateIssueArguments(
                usuario,
                tipo,
                ttl);

        LocalDateTime now =
                utcNow();

        tokenSeguridadUsuarioRepository
                .revokeActiveTokens(
                        usuario.getIdUsuario(),
                        tipo,
                        now);

        String rawToken =
                securityTokenService
                        .generateOpaqueToken();

        TokenSeguridadUsuario token =
                new TokenSeguridadUsuario();

        token.setUsuario(usuario);
        token.setTipo(tipo);
        token.setTokenHash(
                securityTokenService
                        .hashToken(rawToken));
        token.setFechaCreacion(now);
        token.setFechaExpiracion(
                now.plus(ttl));
        token.setFechaUso(null);
        token.setFechaRevocacion(null);

        tokenSeguridadUsuarioRepository.save(
                token);

        return new IssuedToken(
                rawToken,
                token.getFechaExpiracion());
    }

    @Transactional
    public Usuario resolveActive(
            String rawToken,
            TipoTokenSeguridad tipo) {

        if (rawToken == null
                || rawToken.isBlank()
                || tipo == null) {

            throw new TokenSeguridadInvalidoException();
        }

        String tokenHash =
                securityTokenService
                        .hashToken(rawToken);

        TokenSeguridadUsuario token =
                tokenSeguridadUsuarioRepository
                        .findForUpdate(
                                tokenHash,
                                tipo)
                        .orElseThrow(
                                TokenSeguridadInvalidoException::new);

        LocalDateTime now =
                utcNow();

        if (token.getFechaUso() != null
                || token.getFechaRevocacion() != null
                || token.getFechaExpiracion() == null
                || !token.getFechaExpiracion()
                        .isAfter(now)) {

            throw new TokenSeguridadInvalidoException();
        }

        return token.getUsuario();
    }

    @Transactional
    public Usuario consume(
            String rawToken,
            TipoTokenSeguridad tipo) {

        if (rawToken == null
                || rawToken.isBlank()
                || tipo == null) {

            throw new TokenSeguridadInvalidoException();
        }

        String tokenHash =
                securityTokenService
                        .hashToken(rawToken);

        TokenSeguridadUsuario token =
                tokenSeguridadUsuarioRepository
                        .findForUpdate(
                                tokenHash,
                                tipo)
                        .orElseThrow(
                                TokenSeguridadInvalidoException::new);

        LocalDateTime now =
                utcNow();

        if (token.getFechaUso() != null
                || token.getFechaRevocacion() != null
                || token.getFechaExpiracion() == null
                || !token.getFechaExpiracion()
                        .isAfter(now)) {

            throw new TokenSeguridadInvalidoException();
        }

        token.setFechaUso(now);

        tokenSeguridadUsuarioRepository.save(
                token);

        return token.getUsuario();
    }

    @Transactional
    public int revokeActive(
            Usuario usuario,
            TipoTokenSeguridad tipo) {

        if (usuario == null
                || usuario.getIdUsuario() == null
                || tipo == null) {

            throw new IllegalArgumentException(
                    "Usuario y tipo son requeridos");
        }

        return tokenSeguridadUsuarioRepository
                .revokeActiveTokens(
                        usuario.getIdUsuario(),
                        tipo,
                        utcNow());
    }

    private void validateIssueArguments(
            Usuario usuario,
            TipoTokenSeguridad tipo,
            Duration ttl) {

        if (usuario == null
                || usuario.getIdUsuario() == null) {

            throw new IllegalArgumentException(
                    "Usuario persistido requerido");
        }

        if (tipo == null) {
            throw new IllegalArgumentException(
                    "Tipo de token requerido");
        }

        if (ttl == null
                || ttl.isZero()
                || ttl.isNegative()) {

            throw new IllegalArgumentException(
                    "TTL debe ser mayor que cero");
        }
    }

    private LocalDateTime utcNow() {
        return LocalDateTime.now(
                ZoneOffset.UTC);
    }

    public record IssuedToken(
            String rawToken,
            LocalDateTime expiresAt) {
    }
}
