package com.bembosevents.repository;

import com.bembosevents.entity.EventoPagoProveedor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventoPagoProveedorRepository
        extends JpaRepository<EventoPagoProveedor, Long> {

    boolean existsByProveedorAndIdEventoExterno(
            String proveedor,
            String idEventoExterno);
    java.util.Optional<EventoPagoProveedor> findByProveedorAndIdEventoExterno(
            String proveedor,
            String idEventoExterno);
}