package com.bembosevents.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "UsuarioSeguridad")
@Getter
@Setter
@NoArgsConstructor
public class UsuarioSeguridad {

    @Id
    private Integer idUsuario;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idUsuario", nullable = false)
    private Usuario usuario;

    @Column(name = "correoVerificado", nullable = false)
    private Boolean correoVerificado;

    @Column(name = "intentosFallidos", nullable = false)
    private Integer intentosFallidos;

    @Column(name = "bloqueadoHasta")
    private LocalDateTime bloqueadoHasta;

    @Column(name = "mfaHabilitado", nullable = false)
    private Boolean mfaHabilitado;

    @Column(name = "totpSecretProtegido", length = 1000)
    private String totpSecretProtegido;

    @Column(name = "fechaCreacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fechaActualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();

        if (correoVerificado == null) {
            correoVerificado = false;
        }

        if (intentosFallidos == null) {
            intentosFallidos = 0;
        }

        if (mfaHabilitado == null) {
            mfaHabilitado = false;
        }

        if (fechaCreacion == null) {
            fechaCreacion = now;
        }

        fechaActualizacion = now;
    }

    @PreUpdate
    void preUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}
