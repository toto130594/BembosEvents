package com.bembosevents.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "Sede")
@Getter
@Setter
@NoArgsConstructor
public class Sede {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idSede;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "direccion", nullable = false, length = 250)
    private String direccion;

    @Column(name = "distrito", nullable = false, length = 100)
    private String distrito;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "activo", nullable = false)
    private Boolean activo;

    @OneToMany(mappedBy = "sede")
    private List<Salon> salones;

    @OneToMany(mappedBy = "sede")
    private List<Reserva> reservas;

    // Getters y Setters















}
