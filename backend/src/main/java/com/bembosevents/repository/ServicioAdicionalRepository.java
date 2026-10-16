package com.bembosevents.repository;

import com.bembosevents.entity.ServicioAdicional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServicioAdicionalRepository
        extends JpaRepository<ServicioAdicional, Integer> {

    List<ServicioAdicional> findByActivoTrueOrderByNombreAsc();

    Optional<ServicioAdicional> findByIdServicioAdicionalAndActivoTrue(
            Integer idServicioAdicional);
}
