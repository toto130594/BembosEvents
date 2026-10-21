package com.bembosevents.repository;

import com.bembosevents.entity.Conversacion;
import com.bembosevents.entity.EstadoConversacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConversacionRepository
        extends JpaRepository<Conversacion, Integer> {

    List<Conversacion>
    findByCliente_IdClienteOrderByFechaActualizacionDesc(
            Integer idCliente
    );

    Optional<Conversacion>
    findByIdConversacionAndCliente_IdCliente(
            Integer idConversacion,
            Integer idCliente
    );

    List<Conversacion>
    findBySede_IdSedeAndEstadoInOrderByFechaActualizacionAsc(
            Integer idSede,
            List<EstadoConversacion> estados
    );

    List<Conversacion>
    findAllByOrderByFechaActualizacionDesc();

    List<Conversacion>
    findBySede_IdSedeOrderByFechaActualizacionDesc(
            Integer idSede
    );
}