package com.bembosevents.service.impl;

import com.bembosevents.dto.auth.AuthResponse;
import com.bembosevents.dto.auth.SesionResponse;
import com.bembosevents.dto.auth.LoginRequest;
import com.bembosevents.dto.auth.LogoutRequest;
import com.bembosevents.dto.auth.RefreshRequest;
import com.bembosevents.dto.auth.RegistroRequest;
import com.bembosevents.entity.Cliente;
import com.bembosevents.entity.Rol;
import com.bembosevents.entity.Usuario;
import com.bembosevents.entity.UsuarioSeguridad;
import com.bembosevents.entity.UsuarioRol;
import com.bembosevents.repository.ClienteRepository;
import com.bembosevents.repository.RolRepository;
import com.bembosevents.repository.UsuarioRepository;
import com.bembosevents.repository.UsuarioSeguridadRepository;
import com.bembosevents.repository.UsuarioRolRepository;
import com.bembosevents.security.JwtService;
import com.bembosevents.security.UsuarioDetailsService;
import com.bembosevents.service.LoginSecurityService;
import com.bembosevents.service.MfaLoginChallengeService;
import com.bembosevents.service.SesionUsuarioService;
import com.bembosevents.service.AuthService;
import com.bembosevents.service.AuthSessionIssuanceService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class AuthServiceImpl implements AuthService {

    private static final String ROL_CLIENTE = "CLIENTE";

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final UsuarioRolRepository usuarioRolRepository;
    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UsuarioDetailsService usuarioDetailsService;
    private final JwtService jwtService;
    private final LoginSecurityService loginSecurityService;
    private final UsuarioSeguridadRepository usuarioSeguridadRepository;
    private final MfaLoginChallengeService mfaLoginChallengeService;

    private final AuthSessionIssuanceService
            authSessionIssuanceService;

    private final SesionUsuarioService
            sesionUsuarioService;
    public AuthServiceImpl(
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            UsuarioRolRepository usuarioRolRepository,
            ClienteRepository clienteRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            UsuarioDetailsService usuarioDetailsService,
            JwtService jwtService,
            LoginSecurityService loginSecurityService,
            UsuarioSeguridadRepository usuarioSeguridadRepository,
            MfaLoginChallengeService mfaLoginChallengeService,
            AuthSessionIssuanceService authSessionIssuanceService,
            SesionUsuarioService sesionUsuarioService) {

        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.usuarioRolRepository = usuarioRolRepository;
        this.clienteRepository = clienteRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.usuarioDetailsService = usuarioDetailsService;
        this.jwtService = jwtService;
        this.loginSecurityService = loginSecurityService;
        this.usuarioSeguridadRepository = usuarioSeguridadRepository;
        this.mfaLoginChallengeService = mfaLoginChallengeService;

        this.authSessionIssuanceService =
                authSessionIssuanceService;

        this.sesionUsuarioService =
                sesionUsuarioService;
    }

    @Override
    @Transactional
    public void registrar(RegistroRequest request) {

        String correo =
                normalizarCorreo(request.correo());

        if (usuarioRepository
                .existsByCorreoIgnoreCase(correo)) {

            throw new IllegalArgumentException(
                    "Ya existe un usuario con ese correo");
        }

        Rol rolCliente = rolRepository
                .findByNombreIgnoreCase(ROL_CLIENTE)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "El rol CLIENTE no esta configurado"));

        if (!Boolean.TRUE.equals(
                rolCliente.getActivo())) {

            throw new IllegalStateException(
                    "El rol CLIENTE esta inactivo");
        }

        LocalDateTime now =
                LocalDateTime.now();

        Usuario usuario = new Usuario();

        usuario.setNombres(
                request.nombres().trim());

        usuario.setApellidos(
                request.apellidos().trim());

        usuario.setCorreo(correo);

        usuario.setTelefono(
                request.telefono() == null
                        ? null
                        : request.telefono().trim());

        usuario.setPasswordHash(
                passwordEncoder.encode(
                        request.password()));

        usuario.setActivo(true);
        usuario.setFechaCreacion(now);
        usuario.setFechaActualizacion(now);

        Usuario usuarioGuardado =
                usuarioRepository.save(usuario);

        UsuarioRol usuarioRol =
                new UsuarioRol();

        usuarioRol.setUsuario(
                usuarioGuardado);

        usuarioRol.setRol(
                rolCliente);

        usuarioRol.setFechaAsignacion(now);

        usuarioRolRepository.save(
                usuarioRol);

        Cliente cliente = new Cliente();

        cliente.setUsuario(
                usuarioGuardado);

        cliente.setDocumentoIdentidad(null);

        cliente.setFechaRegistro(
                LocalDate.now());

        cliente.setActivo(true);

        clienteRepository.save(cliente);

        loginSecurityService
                .initializeForCurrentRegistration(
                        usuarioGuardado);
    }

    @Override
    public AuthResponse login(
            LoginRequest request) {

        String correo =
                normalizarCorreo(request.correo());

        try {
            loginSecurityService
                    .validateLoginAllowed(
                            correo);
        } catch (AuthenticationException ex) {

            loginSecurityService
                    .recordRejected(
                            correo);

            throw new BadCredentialsException(
                    "Credenciales invalidas");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            correo,
                            request.password()));
        } catch (BadCredentialsException ex) {

            loginSecurityService
                    .recordFailure(
                            correo);

            throw new BadCredentialsException(
                    "Credenciales invalidas");
        } catch (AuthenticationException ex) {

            loginSecurityService
                    .recordRejected(
                            correo);

            throw new BadCredentialsException(
                    "Credenciales invalidas");
        }

        UsuarioSeguridad seguridad =
                usuarioSeguridadRepository
                        .findByUsuario_CorreoIgnoreCase(
                                correo)
                        .orElseThrow(
                                () ->
                                        new BadCredentialsException(
                                                "Credenciales invalidas"));

        Usuario usuario =
                usuarioRepository
                        .findByCorreoIgnoreCase(
                                correo)
                        .orElseThrow(
                                () ->
                                        new BadCredentialsException(
                                                "Credenciales invalidas"));

        if (Boolean.TRUE.equals(
                seguridad.getMfaHabilitado())) {

            MfaLoginChallengeService.IssuedChallenge challenge =
                    mfaLoginChallengeService.issue(
                            usuario);

            return AuthResponse.mfaRequired(
                    challenge.rawToken(),
                    mfaLoginChallengeService
                            .getChallengeTtlMs());
        }
        AuthResponse response =
                authSessionIssuanceService
                        .issue(
                                usuario);

        loginSecurityService
                .recordSuccess(
                        correo);

        return response;
    }
    @Override
    public AuthResponse refresh(
            RefreshRequest request) {

        return authSessionIssuanceService
                .refresh(
                        request.refreshToken());
    }

    @Override
    public void logout(
            LogoutRequest request) {

        sesionUsuarioService
                .revocar(
                        request.refreshToken());
    }

    @Override
    public void logoutAll(
            String correo) {

        if (correo == null
                || correo.isBlank()) {

            throw new BadCredentialsException(
                    "Credenciales invalidas");
        }

        String correoNormalizado =
                normalizarCorreo(
                        correo);

        Usuario usuario =
                usuarioRepository
                        .findByCorreoIgnoreCase(
                                correoNormalizado)
                        .orElseThrow(
                                () ->
                                        new BadCredentialsException(
                                                "Credenciales invalidas"));

        sesionUsuarioService
                .revocarTodas(
                        usuario);
    }
    @Override
    @Transactional(readOnly = true)
    public List<SesionResponse> listarSesiones(
            String correo) {

        if (correo == null
                || correo.isBlank()) {

            throw new BadCredentialsException(
                    "Credenciales invalidas");
        }

        String correoNormalizado =
                normalizarCorreo(
                        correo);

        Usuario usuario =
                usuarioRepository
                        .findByCorreoIgnoreCase(
                                correoNormalizado)
                        .orElseThrow(
                                () ->
                                        new BadCredentialsException(
                                                "Credenciales invalidas"));

        return sesionUsuarioService
                .listarActivas(
                        usuario);
    }

    @Override
    @Transactional
    public void revocarSesion(
            String correo,
            Long idSesionUsuario) {

        if (correo == null
                || correo.isBlank()) {

            throw new BadCredentialsException(
                    "Credenciales invalidas");
        }

        String correoNormalizado =
                normalizarCorreo(
                        correo);

        Usuario usuario =
                usuarioRepository
                        .findByCorreoIgnoreCase(
                                correoNormalizado)
                        .orElseThrow(
                                () ->
                                        new BadCredentialsException(
                                                "Credenciales invalidas"));

        sesionUsuarioService
                .revocarPropia(
                        usuario,
                        idSesionUsuario);
    }
    private String normalizarCorreo(
            String correo) {

        return correo
                .trim()
                .toLowerCase(Locale.ROOT);
    }
}
