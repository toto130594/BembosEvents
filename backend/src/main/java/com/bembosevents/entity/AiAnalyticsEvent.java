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
@Table(name = "AiAnalyticsEvent")
@Getter
@Setter
@NoArgsConstructor
public class AiAnalyticsEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idAiAnalyticsEvent")
    private Long idAiAnalyticsEvent;

    @Column(name = "idConversacion", nullable = false)
    private Integer idConversacion;

    @Column(name = "idSede")
    private Integer idSede;

    @Column(name = "intentType", nullable = false, length = 60)
    private String intentType;

    @Column(name = "toolType", length = 60)
    private String toolType;

    @Column(name = "executionRoute", nullable = false, length = 20)
    private String executionRoute;

    @Column(name = "exitoso", nullable = false)
    private Boolean exitoso;

    @Column(name = "durationMs", nullable = false)
    private Long durationMs;

    @Column(name = "errorType", length = 120)
    private String errorType;

    @Column(name = "fechaEvento", nullable = false)
    private LocalDateTime fechaEvento;
}