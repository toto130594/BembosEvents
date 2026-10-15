package com.bembosevents.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "Reserva")
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idReserva;

    @Column(name = "codigoReserva", nullable = false, unique = true, length = 30)
    private String codigoReserva;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idCliente", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idSede", nullable = false)
    private Sede sede;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idSalon", nullable = false)
    private Salon salon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idPaquete")
    private Paquete paquete;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idTematica")
    private Tematica tematica;

    @Column(name = "fechaEvento", nullable = false)
    private LocalDate fechaEvento;

    @Column(name = "horaInicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "horaFin", nullable = false)
    private LocalTime horaFin;

    @Column(name = "cantidadInvitados", nullable = false)
    private Integer cantidadInvitados;

    @Column(name = "montoBase", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoBase;

    @Column(name = "montoServicios", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoServicios;

    @Column(name = "montoTotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoTotal;

    @Column(name = "porcentajeDeposito", nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentajeDeposito;

    @Column(name = "montoDeposito", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoDeposito;

    @Column(name = "estado", nullable = false, length = 30)
    private String estado;

    @Column(name = "fechaCreacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fechaActualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @OneToMany(mappedBy = "reserva")
    private List<ReservaServicio> reservaServicios;

    @OneToMany(mappedBy = "reserva")
    private List<Pago> pagos;

    @OneToMany(mappedBy = "reserva")
    private List<HistorialReserva> historialReservas;

    // Getters y Setters
    public Integer getIdReserva() {
        return idReserva;
    }

    public void setIdReserva(Integer idReserva) {
        this.idReserva = idReserva;
    }

    public String getCodigoReserva() {
        return codigoReserva;
    }

    public void setCodigoReserva(String codigoReserva) {
        this.codigoReserva = codigoReserva;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Sede getSede() {
        return sede;
    }

    public void setSede(Sede sede) {
        this.sede = sede;
    }

    public Salon getSalon() {
        return salon;
    }

    public void setSalon(Salon salon) {
        this.salon = salon;
    }

    public Paquete getPaquete() {
        return paquete;
    }

    public void setPaquete(Paquete paquete) {
        this.paquete = paquete;
    }

    public Tematica getTematica() {
        return tematica;
    }

    public void setTematica(Tematica tematica) {
        this.tematica = tematica;
    }

    public LocalDate getFechaEvento() {
        return fechaEvento;
    }

    public void setFechaEvento(LocalDate fechaEvento) {
        this.fechaEvento = fechaEvento;
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

    public Integer getCantidadInvitados() {
        return cantidadInvitados;
    }

    public void setCantidadInvitados(Integer cantidadInvitados) {
        this.cantidadInvitados = cantidadInvitados;
    }

    public BigDecimal getMontoBase() {
        return montoBase;
    }

    public void setMontoBase(BigDecimal montoBase) {
        this.montoBase = montoBase;
    }

    public BigDecimal getMontoServicios() {
        return montoServicios;
    }

    public void setMontoServicios(BigDecimal montoServicios) {
        this.montoServicios = montoServicios;
    }

    public BigDecimal getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(BigDecimal montoTotal) {
        this.montoTotal = montoTotal;
    }

    public BigDecimal getPorcentajeDeposito() {
        return porcentajeDeposito;
    }

    public void setPorcentajeDeposito(BigDecimal porcentajeDeposito) {
        this.porcentajeDeposito = porcentajeDeposito;
    }

    public BigDecimal getMontoDeposito() {
        return montoDeposito;
    }

    public void setMontoDeposito(BigDecimal montoDeposito) {
        this.montoDeposito = montoDeposito;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public List<ReservaServicio> getReservaServicios() {
        return reservaServicios;
    }

    public void setReservaServicios(List<ReservaServicio> reservaServicios) {
        this.reservaServicios = reservaServicios;
    }

    public List<Pago> getPagos() {
        return pagos;
    }

    public void setPagos(List<Pago> pagos) {
        this.pagos = pagos;
    }

    public List<HistorialReserva> getHistorialReservas() {
        return historialReservas;
    }

    public void setHistorialReservas(List<HistorialReserva> historialReservas) {
        this.historialReservas = historialReservas;
    }
}
