package com.bembosevents.service.impl;

import com.bembosevents.dto.usuario.CrearUsuarioRequest;
import com.bembosevents.entity.*;
import com.bembosevents.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

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
}