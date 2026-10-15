package com.bembosevents.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "Salon")
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
    public Integer getIdSalon() {
        return idSalon;
    }

    public void setIdSalon(Integer idSalon) {
        this.idSalon = idSalon;
    }

    public Sede getSede() {
        return sede;
    }

    public void setSede(Sede sede) {
        this.sede = sede;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Integer getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(Integer capacidad) {
        this.capacidad = capacidad;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public List<HorarioSalon> getHorarios() {
        return horarios;
    }

    public void setHorarios(List<HorarioSalon> horarios) {
        this.horarios = horarios;
    }

    public List<Reserva> getReservas() {
        return reservas;
    }

    public void setReservas(List<Reserva> reservas) {
        this.reservas = reservas;
    }
}
