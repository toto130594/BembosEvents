package com.bembosevents.repository;

import com.bembosevents.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

    Optional<Cliente> findByUsuario_IdUsuario(
            Integer idUsuario);

    Optional<Cliente> findByUsuario_CorreoIgnoreCaseAndActivoTrue(
            String correo);
}
