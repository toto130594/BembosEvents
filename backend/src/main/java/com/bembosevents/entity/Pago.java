package com.bembosevents.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Pago")
@Getter
@Setter
@NoArgsConstructor
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idPago;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idReserva", nullable = false)
    private Reserva reserva;

    @Column(name = "monto", nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Column(name = "metodoPago", nullable = false, length = 50)
    private String metodoPago;

    @Column(name = "estadoPago", nullable = false, length = 30)
    private String estadoPago;

    @Column(name = "referenciaExterna", length = 150)
    private String referenciaExterna;

    @Column(name = "fechaPago")
    private LocalDateTime fechaPago;

    @Column(name = "fechaCreacion", nullable = false)
    private LocalDateTime fechaCreacion;

    // Getters y Setters















}
