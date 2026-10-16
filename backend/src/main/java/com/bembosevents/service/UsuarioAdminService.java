package com.bembosevents.service;

import com.bembosevents.dto.usuario.*;

import java.util.List;

public interface UsuarioAdminService {

    UsuarioResponse crear(CrearUsuarioRequest request);

    List<UsuarioResponse> listar();

    UsuarioResponse obtenerPorId(Integer idUsuario);

    UsuarioResponse actualizar(
            Integer idUsuario,
            ActualizarUsuarioRequest request
    );

    UsuarioResponse cambiarRol(
            Integer idUsuario,
            ActualizarRolUsuarioRequest request
    );

    UsuarioResponse cambiarEstado(
            Integer idUsuario,
            ActualizarEstadoUsuarioRequest request
    );
}