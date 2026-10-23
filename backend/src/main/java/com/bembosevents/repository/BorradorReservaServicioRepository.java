package com.bembosevents.repository;

import com.bembosevents.entity.BorradorReservaServicio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BorradorReservaServicioRepository
        extends JpaRepository<BorradorReservaServicio, Integer> {

    List<BorradorReservaServicio>
    findByIdBorradorOrderByIdBorradorReservaServicioAsc(
            Integer idBorrador);

    void deleteByIdBorrador(
            Integer idBorrador);
}