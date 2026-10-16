package com.bembosevents.repository;

import com.bembosevents.entity.Tematica;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TematicaRepository
        extends JpaRepository<Tematica, Integer> {

    List<Tematica> findByActivoTrueOrderByNombreAsc();

    Optional<Tematica> findByIdTematicaAndActivoTrue(
            Integer idTematica);
}
