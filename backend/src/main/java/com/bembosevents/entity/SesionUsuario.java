package com.bembosevents.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "SesionUsuario")
@Getter
@Setter
@NoArgsConstructor
public class SesionUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idSesionUsuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idUsuario", nullable = false)
    private Usuario usuario;

    @Column(name = "refreshTokenHash", nullable = false, length = 64)
    private String refreshTokenHash;

    @Column(name = "fechaCreacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fechaExpiracion", nullable = false)
    private LocalDateTime fechaExpiracion;

    @Column(name = "fechaUltimoUso", nullable = false)
    private LocalDateTime fechaUltimoUso;

    @Column(name = "fechaRevocacion")
    private LocalDateTime fechaRevocacion;

    @Column(name = "userAgent", length = 500)
    private String userAgent;

    @Column(name = "ipOrigen", length = 64)
    private String ipOrigen;

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();

        if (fechaCreacion == null) {
            fechaCreacion = now;
        }

        if (fechaUltimoUso == null) {
            fechaUltimoUso = now;
        }
    }
}
