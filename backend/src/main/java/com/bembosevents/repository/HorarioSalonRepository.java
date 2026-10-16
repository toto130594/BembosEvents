package com.bembosevents.repository;

import com.bembosevents.entity.HorarioSalon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HorarioSalonRepository
        extends JpaRepository<HorarioSalon, Integer> {

    List<HorarioSalon> findBySalon_IdSalonAndActivoTrue(
            Integer idSalon);
}
