package com.bembosevents.repository;

import com.bembosevents.entity.Salon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SalonRepository extends JpaRepository<Salon, Integer> {

    List<Salon> findBySede_IdSedeAndActivoTrueOrderByNombreAsc(
            Integer idSede);

    Optional<Salon> findByIdSalonAndActivoTrue(
            Integer idSalon);
}
