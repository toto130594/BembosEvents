package com.bembosevents.repository;

import com.bembosevents.entity.Salon;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SalonRepository
        extends JpaRepository<Salon, Integer> {

    List<Salon> findBySede_IdSedeAndActivoTrueOrderByNombreAsc(
            Integer idSede);

    Optional<Salon> findByIdSalonAndActivoTrue(
            Integer idSalon);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
            "select s from Salon s " +
            "join fetch s.sede " +
            "where s.idSalon = :idSalon " +
            "and s.activo = true")
    Optional<Salon> findByIdSalonAndActivoTrueForUpdate(
            @Param("idSalon") Integer idSalon);
}
