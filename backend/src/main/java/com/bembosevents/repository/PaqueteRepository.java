package com.bembosevents.repository;

import com.bembosevents.entity.Paquete;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaqueteRepository
        extends JpaRepository<Paquete, Integer> {

    List<Paquete> findByActivoTrueOrderByNombreAsc();

    Optional<Paquete> findByIdPaqueteAndActivoTrue(
            Integer idPaquete);
}
