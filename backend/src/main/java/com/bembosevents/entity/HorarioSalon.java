package com.bembosevents.entity;

import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "HorarioSalon")
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
    public Integer getIdHorarioSalon() {
        return idHorarioSalon;
    }

    public void setIdHorarioSalon(Integer idHorarioSalon) {
        this.idHorarioSalon = idHorarioSalon;
    }

    public Salon getSalon() {
        return salon;
    }

    public void setSalon(Salon salon) {
        this.salon = salon;
    }

    public String getDiaSemana() {
        return diaSemana;
    }

    public void setDiaSemana(String diaSemana) {
        this.diaSemana = diaSemana;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(LocalTime horaFin) {
        this.horaFin = horaFin;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
