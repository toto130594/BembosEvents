package com.bembosevents.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "HistorialReserva")
@Getter
@Setter
@NoArgsConstructor
public class HistorialReserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idHistorialReserva;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idReserva", nullable = false)
    private Reserva reserva;

    @Column(name = "estadoAnterior", length = 30)
    private String estadoAnterior;

    @Column(name = "estadoNuevo", nullable = false, length = 30)
    private String estadoNuevo;

    @Column(name = "observacion", length = 500)
    private String observacion;

    @Column(name = "fechaCambio", nullable = false)
    private LocalDateTime fechaCambio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idUsuarioResponsable")
    private Usuario usuarioResponsable;

    // Getters y Setters













}
