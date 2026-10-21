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

import java.time.LocalDateTime;

@Entity
@Table(name = "EventoPagoProveedor")
@Getter
@Setter
@NoArgsConstructor
public class EventoPagoProveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEventoPagoProveedor;

    @Column(nullable = false, length = 40)
    private String proveedor;

    @Column(nullable = false, length = 200)
    private String idEventoExterno;

    @Column(nullable = false, length = 150)
    private String idRecursoExterno;

    @Column(length = 80)
    private String tipoEvento;

    @Column(length = 120)
    private String accion;

    @Column(length = 150)
    private String requestId;

    private Boolean liveMode;

    @Column(nullable = false)
    private LocalDateTime fechaRecepcion;

    @Column(nullable = false)
    private Boolean procesado;

    private LocalDateTime fechaProcesamiento;

    @Column(nullable = false, length = 50)
    private String resultado;
}