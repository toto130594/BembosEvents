package com.bembosevents.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "Paquete")
@Getter
@Setter
@NoArgsConstructor
public class Paquete {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idPaquete;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "precioBase", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioBase;

    @Column(name = "cantidadPersonasBase", nullable = false)
    private Integer cantidadPersonasBase;

    @Column(name = "activo", nullable = false)
    private Boolean activo;

    @OneToMany(mappedBy = "paquete")
    private List<Reserva> reservas;

    // Getters y Setters













}
