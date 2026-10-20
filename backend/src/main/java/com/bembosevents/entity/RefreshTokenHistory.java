package com.bembosevents.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "RefreshTokenHistory")
@Getter
@Setter
@NoArgsConstructor
public class RefreshTokenHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idRefreshTokenHistory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "idSesionUsuario",
            nullable = false)
    private SesionUsuario sesionUsuario;

    @Column(
            name = "refreshTokenHash",
            nullable = false,
            length = 64)
    private String refreshTokenHash;

    @Column(
            name = "fechaRotacion",
            nullable = false)
    private LocalDateTime fechaRotacion;

    @Column(
            name = "fechaExpiracion",
            nullable = false)
    private LocalDateTime fechaExpiracion;
}