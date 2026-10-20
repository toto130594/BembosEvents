package com.bembosevents.service;

import com.bembosevents.dto.auth.AuthResponse;
import com.bembosevents.entity.Usuario;
import com.bembosevents.security.JwtService;
import com.bembosevents.security.UsuarioDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class AuthSessionIssuanceService {

    private final UsuarioDetailsService
            usuarioDetailsService;

    private final JwtService jwtService;

    private final SesionUsuarioService
            sesionUsuarioService;

    public AuthSessionIssuanceService(
            UsuarioDetailsService usuarioDetailsService,
            JwtService jwtService,
            SesionUsuarioService sesionUsuarioService) {

        this.usuarioDetailsService =
                usuarioDetailsService;

        this.jwtService =
                jwtService;

        this.sesionUsuarioService =
                sesionUsuarioService;
    }

    @Transactional
    public AuthResponse issue(
            Usuario usuario) {

        if (usuario == null
                || usuario.getIdUsuario() == null
                || usuario.getCorreo() == null
                || usuario.getCorreo().isBlank()) {

            throw new IllegalArgumentException(
                    "Usuario persistido con correo requerido");
        }

        SesionUsuarioService.IssuedRefreshToken
                refreshToken =
                sesionUsuarioService.crear(
                        usuario,
                        null,
                        null);

        UserDetails userDetails =
                usuarioDetailsService
                        .loadUserByUsername(
                                usuario.getCorreo());

        String accessToken =
                jwtService.generateToken(
                        userDetails);

        long refreshExpiresInMs =
                remainingMillis(
                        refreshToken.expiresAt());

        return AuthResponse.authenticated(
                accessToken,
                "Bearer",
                jwtService.getExpirationMs(),
                refreshToken.rawRefreshToken(),
                refreshExpiresInMs);
    }


    @Transactional(noRollbackFor = BadCredentialsException.class)
    public AuthResponse refresh(
            String rawRefreshToken) {

        SesionUsuarioService.IssuedRefreshToken
                rotatedRefreshToken =
                sesionUsuarioService.rotar(
                        rawRefreshToken);

        Usuario usuario =
                rotatedRefreshToken.usuario();

        if (usuario == null
                || usuario.getIdUsuario() == null
                || usuario.getCorreo() == null
                || usuario.getCorreo().isBlank()) {

            throw new IllegalStateException(
                    "La sesion rotada no contiene un usuario valido");
        }

        UserDetails userDetails =
                usuarioDetailsService
                        .loadUserByUsername(
                                usuario.getCorreo());

        String accessToken =
                jwtService.generateToken(
                        userDetails);

        long refreshExpiresInMs =
                remainingMillis(
                        rotatedRefreshToken
                                .expiresAt());

        return AuthResponse.authenticated(
                accessToken,
                "Bearer",
                jwtService.getExpirationMs(),
                rotatedRefreshToken
                        .rawRefreshToken(),
                refreshExpiresInMs);
    }
    private long remainingMillis(
            LocalDateTime expiresAt) {

        if (expiresAt == null) {
            throw new IllegalStateException(
                    "La expiracion del refresh token es requerida");
        }

        long millis =
                Duration.between(
                        LocalDateTime.now(
                                ZoneOffset.UTC),
                        expiresAt)
                        .toMillis();

        if (millis < 1) {
            throw new IllegalStateException(
                    "Refresh token emitido con expiracion invalida");
        }

        return millis;
    }
}