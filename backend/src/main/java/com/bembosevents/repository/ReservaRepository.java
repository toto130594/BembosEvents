package com.bembosevents.repository;

import com.bembosevents.entity.Reserva;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    List<Reserva> findAllByOrderByFechaCreacionDesc();

    List<Reserva> findBySede_IdSedeOrderByFechaCreacionDesc(
            Integer idSede);

    Optional<Reserva> findByIdReservaAndSede_IdSede(
            Integer idReserva,
            Integer idSede);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT r
            FROM Reserva r
            WHERE r.idReserva = :idReserva
            """)
    Optional<Reserva> findByIdReservaForUpdate(
            @Param("idReserva")
            Integer idReserva);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT r
            FROM Reserva r
            WHERE r.idReserva = :idReserva
              AND r.sede.idSede = :idSede
            """)
    Optional<Reserva> findByIdReservaAndSede_IdSedeForUpdate(
            @Param("idReserva")
            Integer idReserva,
            @Param("idSede")
            Integer idSede);

    @Query(
            value = "SELECT NEXT VALUE FOR dbo.ReservaCodigoSequence",
            nativeQuery = true)
    Long siguienteValorCodigoReserva();
}