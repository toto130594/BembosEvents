package com.bembosevents.service.impl;

import com.bembosevents.dto.usuario.*;
import com.bembosevents.entity.*;
import com.bembosevents.repository.*;
import com.bembosevents.service.UsuarioAdminService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class UsuarioAdminServiceImpl implements UsuarioAdminService {

    private static final String CLIENTE = "CLIENTE";
    private static final String ENCARGADO_LOCAL = "ENCARGADO_LOCAL";
    private static final String ADMINISTRADOR = "ADMINISTRADOR";

    private static final Set<String> ROLES_PERMITIDOS = Set.of(
            CLIENTE,
            ENCARGADO_LOCAL,
            ADMINISTRADOR
    );

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final UsuarioRolRepository usuarioRolRepository;
    private final UsuarioSedeRepository usuarioSedeRepository;
    private final SedeRepository sedeRepository;
    private final PasswordEncoder passwordEncoder;

public UsuarioAdminServiceImpl(
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            UsuarioRolRepository usuarioRolRepository,
            UsuarioSedeRepository usuarioSedeRepository,
            SedeRepository sedeRepository,
            PasswordEncoder passwordEncoder) {

        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.usuarioRolRepository = usuarioRolRepository;
        this.usuarioSedeRepository = usuarioSedeRepository;
        this.sedeRepository = sedeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UsuarioResponse crear(CrearUsuarioRequest request) {

        String correo = normalizarCorreo(request.correo());
        String nombreRol = normalizarRol(request.rol());

        if (usuarioRepository.existsByCorreoIgnoreCase(correo)) {
            throw new IllegalArgumentException(
                    "Ya existe un usuario registrado con ese correo"
            );
        }

        Rol rol = obtenerRolActivo(nombreRol);
        Sede sede = validarSedeSegunRol(nombreRol, request.idSede());

        LocalDateTime ahora = LocalDateTime.now();

        Usuario usuario = new Usuario();
        usuario.setNombres(request.nombres().trim());
        usuario.setApellidos(request.apellidos().trim());
        usuario.setCorreo(correo);
        usuario.setTelefono(normalizarTelefono(request.telefono()));
        usuario.setPasswordHash(
                passwordEncoder.encode(request.password())
        );
        usuario.setActivo(true);
        usuario.setFechaCreacion(ahora);
        usuario.setFechaActualizacion(ahora);

        usuario = usuarioRepository.save(usuario);

        UsuarioRol usuarioRol = new UsuarioRol();
        usuarioRol.setUsuario(usuario);
        usuarioRol.setRol(rol);
        usuarioRol.setFechaAsignacion(ahora);

        usuarioRolRepository.save(usuarioRol);

        if (ENCARGADO_LOCAL.equals(nombreRol)) {
            crearAsignacionSede(usuario, sede, ahora);
        }

        return construirResponse(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll()
                .stream()
                .map(this::construirResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPorId(Integer idUsuario) {
        return construirResponse(obtenerUsuario(idUsuario));
    }

    @Override
    public UsuarioResponse actualizar(
            Integer idUsuario,
            ActualizarUsuarioRequest request) {

        Usuario usuario = obtenerUsuario(idUsuario);

        String nuevoCorreo = normalizarCorreo(request.correo());

        Optional<Usuario> usuarioMismoCorreo =
                usuarioRepository.findByCorreoIgnoreCase(nuevoCorreo);

        if (usuarioMismoCorreo.isPresent() &&
                !usuarioMismoCorreo.get()
                        .getIdUsuario()
                        .equals(idUsuario)) {

            throw new IllegalArgumentException(
                    "Ya existe otro usuario registrado con ese correo"
            );
        }

        usuario.setNombres(request.nombres().trim());
        usuario.setApellidos(request.apellidos().trim());
        usuario.setCorreo(nuevoCorreo);
        usuario.setTelefono(normalizarTelefono(request.telefono()));
        usuario.setFechaActualizacion(LocalDateTime.now());

        usuarioRepository.save(usuario);

        return construirResponse(usuario);
    }

    @Override
    public UsuarioResponse cambiarRol(
            Integer idUsuario,
            ActualizarRolUsuarioRequest request) {

        Usuario usuario = obtenerUsuario(idUsuario);

        String nombreRol = normalizarRol(request.rol());

        Rol nuevoRol = obtenerRolActivo(nombreRol);
        Sede nuevaSede = validarSedeSegunRol(
                nombreRol,
                request.idSede()
        );

        List<UsuarioRol> rolesActuales =
                usuarioRolRepository.findByUsuario_IdUsuario(idUsuario);

        usuarioRolRepository.deleteAll(rolesActuales);

        usuarioSedeRepository
                .findByUsuario_IdUsuario(idUsuario)
                .ifPresent(usuarioSedeRepository::delete);

        LocalDateTime ahora = LocalDateTime.now();

        UsuarioRol nuevaAsignacionRol = new UsuarioRol();
        nuevaAsignacionRol.setUsuario(usuario);
        nuevaAsignacionRol.setRol(nuevoRol);
        nuevaAsignacionRol.setFechaAsignacion(ahora);

        usuarioRolRepository.save(nuevaAsignacionRol);

        if (ENCARGADO_LOCAL.equals(nombreRol)) {
            crearAsignacionSede(
                    usuario,
                    nuevaSede,
                    ahora
            );
        }

        usuario.setFechaActualizacion(ahora);
        usuarioRepository.save(usuario);

        return construirResponse(usuario);
    }

    @Override
    public UsuarioResponse cambiarEstado(
            Integer idUsuario,
            ActualizarEstadoUsuarioRequest request) {

        Usuario usuario = obtenerUsuario(idUsuario);

        usuario.setActivo(request.activo());
        usuario.setFechaActualizacion(LocalDateTime.now());

        usuarioRepository.save(usuario);

        return construirResponse(usuario);
    }

    private Usuario obtenerUsuario(Integer idUsuario) {
        return usuarioRepository.findById(idUsuario)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Usuario no encontrado"
                        )
                );
    }

    private Rol obtenerRolActivo(String nombreRol) {

        if (!ROLES_PERMITIDOS.contains(nombreRol)) {
            throw new IllegalArgumentException(
                    "Rol no permitido: " + nombreRol
            );
        }

        Rol rol = rolRepository
                .findByNombreIgnoreCase(nombreRol)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Rol no encontrado: " + nombreRol
                        )
                );

        if (!Boolean.TRUE.equals(rol.getActivo())) {
            throw new IllegalArgumentException(
                    "El rol se encuentra inactivo"
            );
        }

        return rol;
    }

    private Sede validarSedeSegunRol(
            String nombreRol,
            Integer idSede) {

        if (ENCARGADO_LOCAL.equals(nombreRol)) {

            if (idSede == null) {
                throw new IllegalArgumentException(
                        "El ENCARGADO_LOCAL debe tener una sede asignada"
                );
            }

            Sede sede = sedeRepository.findById(idSede)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Sede no encontrada"
                            )
                    );

            if (!Boolean.TRUE.equals(sede.getActivo())) {
                throw new IllegalArgumentException(
                        "La sede asignada debe estar activa"
                );
            }

            return sede;
        }

        if (idSede != null) {
            throw new IllegalArgumentException(
                    "Solo ENCARGADO_LOCAL puede tener una sede asignada"
            );
        }

        return null;
    }

    private void crearAsignacionSede(
            Usuario usuario,
            Sede sede,
            LocalDateTime fecha) {

        UsuarioSede usuarioSede = new UsuarioSede();
        usuarioSede.setUsuario(usuario);
        usuarioSede.setSede(sede);
        usuarioSede.setFechaAsignacion(fecha);
        usuarioSede.setActivo(true);

        usuarioSedeRepository.save(usuarioSede);
    }

    private UsuarioResponse construirResponse(Usuario usuario) {

        List<String> roles = usuarioRolRepository
                .findByUsuario_IdUsuario(usuario.getIdUsuario())
                .stream()
                .map(UsuarioRol::getRol)
                .filter(rol -> Boolean.TRUE.equals(rol.getActivo()))
                .map(Rol::getNombre)
                .sorted()
                .toList();

        Optional<UsuarioSede> asignacion =
                usuarioSedeRepository
                        .findByUsuario_IdUsuarioAndActivoTrue(
                                usuario.getIdUsuario()
                        );

        Integer idSede = asignacion
                .map(UsuarioSede::getSede)
                .map(Sede::getIdSede)
                .orElse(null);

        String nombreSede = asignacion
                .map(UsuarioSede::getSede)
                .map(Sede::getNombre)
                .orElse(null);

        return new UsuarioResponse(
                usuario.getIdUsuario(),
                usuario.getNombres(),
                usuario.getApellidos(),
                usuario.getCorreo(),
                usuario.getTelefono(),
                usuario.getActivo(),
                roles,
                idSede,
                nombreSede
        );
    }

    private String normalizarRol(String rol) {

        if (rol == null || rol.isBlank()) {
            throw new IllegalArgumentException(
                    "El rol es obligatorio"
            );
        }

        return rol.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizarCorreo(String correo) {
        return correo
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private String normalizarTelefono(String telefono) {

        if (telefono == null || telefono.isBlank()) {
            return null;
        }

        return telefono.trim();
    }
}