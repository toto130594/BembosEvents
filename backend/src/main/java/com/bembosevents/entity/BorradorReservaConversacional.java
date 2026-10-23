package com.bembosevents.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "BorradorReservaConversacional")
@Getter
@Setter
@NoArgsConstructor
public class BorradorReservaConversacional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idBorrador")
    private Integer idBorrador;

    @Column(name = "idConversacion", nullable = false, unique = true)
    private Integer idConversacion;

    @Column(name = "idCliente", nullable = false)
    private Integer idCliente;

    @Column(name = "idSede")
    private Integer idSede;

    @Column(name = "idSalon")
    private Integer idSalon;

    @Column(name = "idPaquete")
    private Integer idPaquete;

    @Column(name = "idTematica")
    private Integer idTematica;

    @Column(name = "fechaEvento")
    private LocalDate fechaEvento;

    @Column(name = "horaInicio")
    private LocalTime horaInicio;

    @Column(name = "horaFin")
    private LocalTime horaFin;

    @Column(name = "cantidadInvitados")
    private Integer cantidadInvitados;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoBorradorReservaConversacional estado;

    @Column(name = "idReservaConfirmada")
    private Integer idReservaConfirmada;

    @Column(name = "fechaCreacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fechaActualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @Column(name = "fechaConfirmacion")
    private LocalDateTime fechaConfirmacion;
}