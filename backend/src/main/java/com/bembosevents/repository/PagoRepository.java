package com.bembosevents.repository;

import com.bembosevents.entity.Pago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PagoRepository extends JpaRepository<Pago, Integer> {

    List<Pago> findByReserva_IdReservaOrderByFechaCreacionDesc(
            Integer idReserva);

    Optional<Pago> findByIdPagoAndReserva_Cliente_IdCliente(
            Integer idPago,
            Integer idCliente);

    List<Pago> findAllByOrderByFechaCreacionDesc();

    List<Pago> findByReserva_Sede_IdSedeOrderByFechaCreacionDesc(
            Integer idSede);

    Optional<Pago> findByIdPagoAndReserva_Sede_IdSede(
            Integer idPago,
            Integer idSede);
}