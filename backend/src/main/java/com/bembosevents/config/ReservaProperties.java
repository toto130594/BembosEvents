package com.bembosevents.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@ConfigurationProperties(prefix = "bembos.reserva")
@Getter
@Setter
public class ReservaProperties {

    private BigDecimal montoMinimo = new BigDecimal("500.00");

    private BigDecimal porcentajeDeposito = new BigDecimal("0.50");
}