package com.bembosevents.repository;

import com.bembosevents.entity.UsuarioRol;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UsuarioRolRepository
        extends JpaRepository<UsuarioRol, Integer> {

    @EntityGraph(attributePaths = "rol")
    List<UsuarioRol> findByUsuario_IdUsuario(Integer idUsuario);
}