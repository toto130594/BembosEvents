package com.bembosevents.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "Salon")
@Getter
@Setter
@NoArgsConstructor
public class Salon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idSalon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idSede", nullable = false)
    private Sede sede;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "capacidad", nullable = false)
    private Integer capacidad;

    @Column(name = "activo", nullable = false)
    private Boolean activo;

    @OneToMany(mappedBy = "salon")
    private List<HorarioSalon> horarios;

    @OneToMany(mappedBy = "salon")
    private List<Reserva> reservas;

    // Getters y Setters















}
