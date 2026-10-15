package com.bembosevents.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "ServicioAdicional")
public class ServicioAdicional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idServicioAdicional;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "precio", nullable = false, precision = 12, scale = 2)
    private BigDecimal precio;

    @Column(name = "activo", nullable = false)
    private Boolean activo;

    @OneToMany(mappedBy = "servicioAdicional")
    private List<ReservaServicio> reservaServicios;

    // Getters y Setters
    public Integer getIdServicioAdicional() {
        return idServicioAdicional;
    }

    public void setIdServicioAdicional(Integer idServicioAdicional) {
        this.idServicioAdicional = idServicioAdicional;
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

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public List<ReservaServicio> getReservaServicios() {
        return reservaServicios;
    }

    public void setReservaServicios(List<ReservaServicio> reservaServicios) {
        this.reservaServicios = reservaServicios;
    }
}
