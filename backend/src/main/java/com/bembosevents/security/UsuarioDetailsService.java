package com.bembosevents.security;

import com.bembosevents.entity.Rol;
import com.bembosevents.entity.Usuario;
import com.bembosevents.entity.UsuarioRol;
import com.bembosevents.repository.UsuarioRepository;
import com.bembosevents.repository.UsuarioRolRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioRolRepository usuarioRolRepository;

    public UsuarioDetailsService(
            UsuarioRepository usuarioRepository,
            UsuarioRolRepository usuarioRolRepository) {

        this.usuarioRepository = usuarioRepository;
        this.usuarioRolRepository = usuarioRolRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String correo)
            throws UsernameNotFoundException {

        Usuario usuario = usuarioRepository
                .findByCorreoIgnoreCase(correo)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Usuario no encontrado"));

        List<String> authorities = usuarioRolRepository
                .findByUsuario_IdUsuario(usuario.getIdUsuario())
                .stream()
                .map(UsuarioRol::getRol)
                .filter(rol -> Boolean.TRUE.equals(rol.getActivo()))
                .map(Rol::getNombre)
                .filter(nombre -> nombre != null && !nombre.isBlank())
                .map(nombre ->
                        nombre.trim().toUpperCase(Locale.ROOT))
                .map(nombre ->
                        nombre.startsWith("ROLE_")
                                ? nombre
                                : "ROLE_" + nombre)
                .toList();

        return User.withUsername(usuario.getCorreo())
                .password(usuario.getPasswordHash())
                .disabled(!Boolean.TRUE.equals(usuario.getActivo()))
                .authorities(authorities.toArray(String[]::new))
                .build();
    }
}