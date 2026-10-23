package com.bembosevents.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "BorradorReservaServicio")
@Getter
@Setter
@NoArgsConstructor
public class BorradorReservaServicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idBorradorReservaServicio")
    private Integer idBorradorReservaServicio;

    @Column(name = "idBorrador", nullable = false)
    private Integer idBorrador;

    @Column(name = "idServicioAdicional", nullable = false)
    private Integer idServicioAdicional;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;
}