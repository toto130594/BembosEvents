package com.bembosevents.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "MensajeChat")
@Getter
@Setter
@NoArgsConstructor
public class MensajeChat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idMensajeChat")
    private Integer idMensajeChat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idConversacion", nullable = false)
    private Conversacion conversacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipoEmisor", nullable = false, length = 30)
    private TipoEmisorMensaje tipoEmisor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idUsuarioEmisor")
    private Usuario usuarioEmisor;

    @Column(name = "contenido", nullable = false, length = 4000)
    private String contenido;

    @Column(name = "fechaEnvio", nullable = false)
    private LocalDateTime fechaEnvio;
}