package com.bembosevents.service.impl;

import com.bembosevents.dto.usuario.ActualizarRolUsuarioRequest;
import com.bembosevents.dto.usuario.CrearUsuarioRequest;
import com.bembosevents.entity.*;
import com.bembosevents.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioAdminServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RolRepository rolRepository;

    @Mock
    private UsuarioRolRepository usuarioRolRepository;

    @Mock
    private UsuarioSedeRepository usuarioSedeRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private SedeRepository sedeRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioAdminServiceImpl service;

    @Test
    void crearEncargadoDebeAsignarRolYSede() {

        CrearUsuarioRequest request =
                new CrearUsuarioRequest(
                        "Carlos",
                        "Ramirez",
                        "Carlos@Bembos.Test",
                        "999999999",
                        "Segura123",
                        "ENCARGADO_LOCAL",
                        2
                );

        Rol rol = new Rol();
        rol.setIdRol(2);
        rol.setNombre("ENCARGADO_LOCAL");
        rol.setActivo(true);

        Sede sede = new Sede();
        sede.setIdSede(2);
        sede.setNombre("Plaza Norte");
        sede.setActivo(true);

        when(usuarioRepository
                .existsByCorreoIgnoreCase(
                        "carlos@bembos.test"
                ))
                .thenReturn(false);

        when(rolRepository
                .findByNombreIgnoreCase(
                        "ENCARGADO_LOCAL"
                ))
                .thenReturn(Optional.of(rol));

        when(sedeRepository.findById(2))
                .thenReturn(Optional.of(sede));

        when(passwordEncoder.encode("Segura123"))
                .thenReturn("HASH");

        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(invocation -> {
                    Usuario usuario = invocation.getArgument(0);
                    usuario.setIdUsuario(10);
                    return usuario;
                });

        service.crear(request);

        verify(usuarioRolRepository)
                .save(any(UsuarioRol.class));

        verify(usuarioSedeRepository)
                .save(any(UsuarioSede.class));
    }

    @Test
    void crearEncargadoSinSedeDebeFallar() {

        CrearUsuarioRequest request =
                new CrearUsuarioRequest(
                        "Carlos",
                        "Ramirez",
                        "carlos@bembos.test",
                        "999999999",
                        "Segura123",
                        "ENCARGADO_LOCAL",
                        null
                );

        Rol rol = new Rol();
        rol.setNombre("ENCARGADO_LOCAL");
        rol.setActivo(true);

        when(usuarioRepository
                .existsByCorreoIgnoreCase(
                        "carlos@bembos.test"
                ))
                .thenReturn(false);

        when(rolRepository
                .findByNombreIgnoreCase(
                        "ENCARGADO_LOCAL"
                ))
                .thenReturn(Optional.of(rol));

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.crear(request)
                );

        assertTrue(
                exception.getMessage()
                        .contains("sede")
        );

        verify(usuarioRepository, never())
                .save(any());
    }

    @Test
    void administradorNoDebeTenerSedeAsignada() {

        CrearUsuarioRequest request =
                new CrearUsuarioRequest(
                        "Maria",
                        "Admin",
                        "admin@bembos.test",
                        "999999998",
                        "Segura123",
                        "ADMINISTRADOR",
                        2
                );

        Rol rol = new Rol();
        rol.setNombre("ADMINISTRADOR");
        rol.setActivo(true);

        when(usuarioRepository
                .existsByCorreoIgnoreCase(
                        "admin@bembos.test"
                ))
                .thenReturn(false);

        when(rolRepository
                .findByNombreIgnoreCase(
                        "ADMINISTRADOR"
                ))
                .thenReturn(Optional.of(rol));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.crear(request)
        );

        verify(usuarioSedeRepository, never())
                .save(any());
    }

    @Test
    void crearClienteDesdeAdminDebeCrearPerfilClienteActivo() {

        CrearUsuarioRequest request =
                new CrearUsuarioRequest(
                        "Ana",
                        "Cliente",
                        "ANA@BEMBOS.TEST",
                        "999999991",
                        "Segura123",
                        "CLIENTE",
                        null
                );

        Rol rol = new Rol();
        rol.setIdRol(1);
        rol.setNombre("CLIENTE");
        rol.setActivo(true);

        when(usuarioRepository
                .existsByCorreoIgnoreCase("ana@bembos.test"))
                .thenReturn(false);

        when(rolRepository
                .findByNombreIgnoreCase("CLIENTE"))
                .thenReturn(Optional.of(rol));

        when(passwordEncoder.encode("Segura123"))
                .thenReturn("HASH");

        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(invocation -> {
                    Usuario usuario = invocation.getArgument(0);
                    usuario.setIdUsuario(20);
                    return usuario;
                });

        when(clienteRepository
                .findByUsuario_IdUsuario(20))
                .thenReturn(Optional.empty());

        when(usuarioRolRepository
                .findByUsuario_IdUsuario(20))
                .thenReturn(List.of());

        when(usuarioSedeRepository
                .findByUsuario_IdUsuarioAndActivoTrue(20))
                .thenReturn(Optional.empty());

        service.crear(request);

        verify(clienteRepository)
                .save(argThat(cliente ->
                        cliente.getUsuario() != null
                                && Integer.valueOf(20).equals(
                                        cliente.getUsuario().getIdUsuario())
                                && cliente.getFechaRegistro() != null
                                && Boolean.TRUE.equals(cliente.getActivo())));

        verify(usuarioSedeRepository, never())
                .save(any(UsuarioSede.class));
    }

    @Test
    void cambiarRolAClienteDebeCrearPerfilSiNoExiste() {

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(30);
        usuario.setNombres("Luis");
        usuario.setApellidos("Prueba");
        usuario.setCorreo("luis@bembos.test");
        usuario.setActivo(true);

        Rol rol = new Rol();
        rol.setIdRol(1);
        rol.setNombre("CLIENTE");
        rol.setActivo(true);

        when(usuarioRepository.findById(30))
                .thenReturn(Optional.of(usuario));

        when(rolRepository
                .findByNombreIgnoreCase("CLIENTE"))
                .thenReturn(Optional.of(rol));

        when(usuarioRolRepository
                .findByUsuario_IdUsuario(30))
                .thenReturn(List.of());

        when(usuarioSedeRepository
                .findByUsuario_IdUsuario(30))
                .thenReturn(Optional.empty());

        when(usuarioSedeRepository
                .findByUsuario_IdUsuarioAndActivoTrue(30))
                .thenReturn(Optional.empty());

        when(clienteRepository
                .findByUsuario_IdUsuario(30))
                .thenReturn(Optional.empty());

        service.cambiarRol(
                30,
                new ActualizarRolUsuarioRequest(
                        "CLIENTE",
                        null));

        verify(clienteRepository)
                .save(argThat(cliente ->
                        cliente.getUsuario() == usuario
                                && cliente.getFechaRegistro() != null
                                && Boolean.TRUE.equals(cliente.getActivo())));
    }

    @Test
    void cambiarRolAClienteDebeReactivarPerfilExistente() {

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(31);
        usuario.setNombres("Rosa");
        usuario.setApellidos("Prueba");
        usuario.setCorreo("rosa@bembos.test");
        usuario.setActivo(true);

        Rol rol = new Rol();
        rol.setIdRol(1);
        rol.setNombre("CLIENTE");
        rol.setActivo(true);

        Cliente cliente = new Cliente();
        cliente.setUsuario(usuario);
        cliente.setActivo(false);

        when(usuarioRepository.findById(31))
                .thenReturn(Optional.of(usuario));

        when(rolRepository
                .findByNombreIgnoreCase("CLIENTE"))
                .thenReturn(Optional.of(rol));

        when(usuarioRolRepository
                .findByUsuario_IdUsuario(31))
                .thenReturn(List.of());

        when(usuarioSedeRepository
                .findByUsuario_IdUsuario(31))
                .thenReturn(Optional.empty());

        when(usuarioSedeRepository
                .findByUsuario_IdUsuarioAndActivoTrue(31))
                .thenReturn(Optional.empty());

        when(clienteRepository
                .findByUsuario_IdUsuario(31))
                .thenReturn(Optional.of(cliente));

        service.cambiarRol(
                31,
                new ActualizarRolUsuarioRequest(
                        "CLIENTE",
                        null));

        assertTrue(cliente.getActivo());

        verify(clienteRepository)
                .save(cliente);
    }

    @Test
    void cambiarRolDesdeClienteDebeDesactivarPerfil() {

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(32);
        usuario.setNombres("Pedro");
        usuario.setApellidos("Prueba");
        usuario.setCorreo("pedro@bembos.test");
        usuario.setActivo(true);

        Rol rol = new Rol();
        rol.setIdRol(3);
        rol.setNombre("ADMINISTRADOR");
        rol.setActivo(true);

        Cliente cliente = new Cliente();
        cliente.setUsuario(usuario);
        cliente.setActivo(true);

        when(usuarioRepository.findById(32))
                .thenReturn(Optional.of(usuario));

        when(rolRepository
                .findByNombreIgnoreCase("ADMINISTRADOR"))
                .thenReturn(Optional.of(rol));

        when(usuarioRolRepository
                .findByUsuario_IdUsuario(32))
                .thenReturn(List.of());

        when(usuarioSedeRepository
                .findByUsuario_IdUsuario(32))
                .thenReturn(Optional.empty());

        when(usuarioSedeRepository
                .findByUsuario_IdUsuarioAndActivoTrue(32))
                .thenReturn(Optional.empty());

        when(clienteRepository
                .findByUsuario_IdUsuario(32))
                .thenReturn(Optional.of(cliente));

        service.cambiarRol(
                32,
                new ActualizarRolUsuarioRequest(
                        "ADMINISTRADOR",
                        null));

        assertFalse(cliente.getActivo());

        verify(clienteRepository)
                .save(cliente);
    }
}