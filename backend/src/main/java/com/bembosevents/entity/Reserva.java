package com.bembosevents.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "Reserva")
@Getter
@Setter
@NoArgsConstructor
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











































}
