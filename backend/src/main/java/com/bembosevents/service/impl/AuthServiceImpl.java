package com.bembosevents.service.impl;

import com.bembosevents.dto.auth.AuthResponse;
import com.bembosevents.dto.auth.LoginRequest;
import com.bembosevents.dto.auth.RegistroRequest;
import com.bembosevents.entity.Rol;
import com.bembosevents.entity.Usuario;
import com.bembosevents.entity.UsuarioRol;
import com.bembosevents.repository.RolRepository;
import com.bembosevents.repository.UsuarioRepository;
import com.bembosevents.repository.UsuarioRolRepository;
import com.bembosevents.security.JwtService;
import com.bembosevents.security.UsuarioDetailsService;
import com.bembosevents.service.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;

@Service
public class AuthServiceImpl implements AuthService {

    private static final String ROL_CLIENTE = "CLIENTE";

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final UsuarioRolRepository usuarioRolRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UsuarioDetailsService usuarioDetailsService;
    private final JwtService jwtService;

    public AuthServiceImpl(
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            UsuarioRolRepository usuarioRolRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            UsuarioDetailsService usuarioDetailsService,
            JwtService jwtService) {

        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.usuarioRolRepository = usuarioRolRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.usuarioDetailsService = usuarioDetailsService;
        this.jwtService = jwtService;
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
    }

    @Override
    public AuthResponse login(
            LoginRequest request) {

        String correo =
                normalizarCorreo(request.correo());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        correo,
                        request.password()));

        UserDetails userDetails =
                usuarioDetailsService
                        .loadUserByUsername(correo);

        String token =
                jwtService.generateToken(
                        userDetails);

        return new AuthResponse(
                token,
                "Bearer",
                jwtService.getExpirationMs());
    }

    private String normalizarCorreo(
            String correo) {

        return correo
                .trim()
                .toLowerCase(Locale.ROOT);
    }
}