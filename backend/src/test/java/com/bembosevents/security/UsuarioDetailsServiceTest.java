package com.bembosevents.security;

import com.bembosevents.entity.Rol;
import com.bembosevents.entity.Usuario;
import com.bembosevents.entity.UsuarioRol;
import com.bembosevents.repository.UsuarioRepository;
import com.bembosevents.repository.UsuarioRolRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioDetailsServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioRolRepository usuarioRolRepository;

    @InjectMocks
    private UsuarioDetailsService usuarioDetailsService;

    @Test
    void debeCargarUsuarioConRolCliente() {

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1);
        usuario.setCorreo("cliente@bembos.test");
        usuario.setPasswordHash("hash");
        usuario.setActivo(true);

        Rol rol = new Rol();
        rol.setNombre("CLIENTE");
        rol.setActivo(true);

        UsuarioRol usuarioRol = new UsuarioRol();
        usuarioRol.setUsuario(usuario);
        usuarioRol.setRol(rol);

        when(usuarioRepository
                .findByCorreoIgnoreCase(
                        "cliente@bembos.test"))
                .thenReturn(Optional.of(usuario));

        when(usuarioRolRepository
                .findByUsuario_IdUsuario(1))
                .thenReturn(List.of(usuarioRol));

        UserDetails result =
                usuarioDetailsService
                        .loadUserByUsername(
                                "cliente@bembos.test");

        assertEquals(
                "cliente@bembos.test",
                result.getUsername());

        assertTrue(
                result.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority.getAuthority()
                                        .equals("ROLE_CLIENTE")));
    }

    @Test
    void debeRechazarUsuarioInexistente() {

        when(usuarioRepository
                .findByCorreoIgnoreCase(
                        "nadie@bembos.test"))
                .thenReturn(Optional.empty());

        assertThrows(
                UsernameNotFoundException.class,
                () -> usuarioDetailsService
                        .loadUserByUsername(
                                "nadie@bembos.test"));
    }
}