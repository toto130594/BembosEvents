package com.bembosevents.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "HorarioSalon")
@Getter
@Setter
@NoArgsConstructor
public class HorarioSalon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idHorarioSalon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idSalon", nullable = false)
    private Salon salon;

    @Column(name = "diaSemana", nullable = false, length = 20)
    private String diaSemana;

    @Column(name = "horaInicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "horaFin", nullable = false)
    private LocalTime horaFin;

    @Column(name = "activo", nullable = false)
    private Boolean activo;

    // Getters y Setters











}
