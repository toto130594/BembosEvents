package com.bembosevents.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "UsuarioSede",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "UX_UsuarioSede_idUsuario",
            columnNames = "idUsuario"
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
public class UsuarioSede {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idUsuarioSede")
    private Integer idUsuarioSede;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idUsuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idSede", nullable = false)
    private Sede sede;

    @Column(name = "fechaAsignacion", nullable = false)
    private LocalDateTime fechaAsignacion;

    @Column(name = "activo", nullable = false)
    private Boolean activo;
}