package com.bembosevents.controller;

import com.bembosevents.dto.usuario.*;
import com.bembosevents.service.UsuarioAdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class UsuarioController {

    private final UsuarioAdminService usuarioAdminService;

public UsuarioController(
            UsuarioAdminService usuarioAdminService) {

        this.usuarioAdminService = usuarioAdminService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse crear(
            @Valid @RequestBody CrearUsuarioRequest request) {

        return usuarioAdminService.crear(request);
    }

    @GetMapping
    public List<UsuarioResponse> listar() {
        return usuarioAdminService.listar();
    }

    @GetMapping("/{idUsuario}")
    public UsuarioResponse obtenerPorId(
            @PathVariable Integer idUsuario) {

        return usuarioAdminService.obtenerPorId(idUsuario);
    }

    @PutMapping("/{idUsuario}")
    public UsuarioResponse actualizar(
            @PathVariable Integer idUsuario,
            @Valid @RequestBody ActualizarUsuarioRequest request) {

        return usuarioAdminService.actualizar(
                idUsuario,
                request
        );
    }

    @PutMapping("/{idUsuario}/rol")
    public UsuarioResponse cambiarRol(
            @PathVariable Integer idUsuario,
            @Valid @RequestBody ActualizarRolUsuarioRequest request) {

        return usuarioAdminService.cambiarRol(
                idUsuario,
                request
        );
    }

    @PatchMapping("/{idUsuario}/estado")
    public UsuarioResponse cambiarEstado(
            @PathVariable Integer idUsuario,
            @Valid @RequestBody ActualizarEstadoUsuarioRequest request) {

        return usuarioAdminService.cambiarEstado(
                idUsuario,
                request
        );
    }
}