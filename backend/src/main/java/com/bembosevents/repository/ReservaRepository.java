package com.bembosevents.repository;

import com.bembosevents.entity.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface ReservaRepository
        extends JpaRepository<Reserva, Integer> {

    List<Reserva> findBySalon_IdSalonAndFechaEventoAndEstadoIn(
            Integer idSalon,
            LocalDate fechaEvento,
            Collection<String> estados);

    @Query(
            value = "SELECT NEXT VALUE FOR dbo.ReservaCodigoSequence",
            nativeQuery = true)
    Long siguienteValorCodigoReserva();
}
