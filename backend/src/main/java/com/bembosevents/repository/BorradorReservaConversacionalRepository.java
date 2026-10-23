package com.bembosevents.repository;

import com.bembosevents.entity.BorradorReservaConversacional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BorradorReservaConversacionalRepository
        extends JpaRepository<BorradorReservaConversacional, Integer> {

    Optional<BorradorReservaConversacional>
    findByIdConversacionAndIdCliente(
            Integer idConversacion,
            Integer idCliente);
}