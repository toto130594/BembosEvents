package com.bembosevents.repository;

import com.bembosevents.entity.Sede;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SedeRepository extends JpaRepository<Sede, Integer> {

    List<Sede> findByActivoTrueOrderByNombreAsc();

    Optional<Sede> findByIdSedeAndActivoTrue(
            Integer idSede);
}
