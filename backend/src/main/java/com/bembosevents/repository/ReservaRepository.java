package com.bembosevents.repository;

import com.bembosevents.entity.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReservaRepository
        extends JpaRepository<Reserva, Integer> {

    List<Reserva> findBySalon_IdSalonAndFechaEventoAndEstadoIn(
            Integer idSalon,
            LocalDate fechaEvento,
            Collection<String> estados);

    List<Reserva> findByCliente_IdClienteOrderByFechaCreacionDesc(
            Integer idCliente);


    Optional<Reserva> findByIdReservaAndCliente_IdCliente(
            Integer idReserva,
            Integer idCliente);
@Query(
            value = "SELECT NEXT VALUE FOR dbo.ReservaCodigoSequence",
            nativeQuery = true)
    Long siguienteValorCodigoReserva();
}
