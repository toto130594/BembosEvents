package com.bembosevents.entity;

import com.bembosevents.domain.TipoEventoSeguridad;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "AuditoriaSeguridad")
@Getter
@Setter
@NoArgsConstructor
public class AuditoriaSeguridad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAuditoriaSeguridad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idUsuario")
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipoEvento", nullable = false, length = 80)
    private TipoEventoSeguridad tipoEvento;

    @Column(name = "exitoso", nullable = false)
    private Boolean exitoso;

    @Column(name = "fechaEvento", nullable = false)
    private LocalDateTime fechaEvento;

    @Column(name = "ipOrigen", length = 64)
    private String ipOrigen;

    @Column(name = "userAgent", length = 500)
    private String userAgent;

    @Column(name = "detalleSeguro", length = 1000)
    private String detalleSeguro;

    @PrePersist
    void prePersist() {
        if (exitoso == null) {
            exitoso = false;
        }

        if (fechaEvento == null) {
            fechaEvento = LocalDateTime.now();
        }
    }
}
