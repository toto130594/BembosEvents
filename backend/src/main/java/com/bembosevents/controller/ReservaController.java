package com.bembosevents.controller;

import com.bembosevents.dto.common.MensajeResponse;
import com.bembosevents.dto.common.MontoResponse;
import com.bembosevents.dto.reserva.CalculoDepositoRequest;
import com.bembosevents.dto.reserva.CalculoMontoTotalRequest;
import com.bembosevents.dto.reserva.CapacidadReservaRequest;
import com.bembosevents.dto.reserva.DisponibilidadReservaRequest;
import com.bembosevents.dto.reserva.MontoMinimoRequest;
import com.bembosevents.dto.reserva.TransicionReservaRequest;
import com.bembosevents.service.ReservaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @PostMapping("/validar-capacidad")
    public ResponseEntity<MensajeResponse> validarCapacidad(
            @Valid @RequestBody CapacidadReservaRequest request) {

        reservaService.validarCapacidad(
                request.idSalon(),
                request.cantidadInvitados());

        return ResponseEntity.ok(
                new MensajeResponse("Capacidad valida"));
    }

    @PostMapping("/validar-disponibilidad")
    public ResponseEntity<MensajeResponse> validarDisponibilidad(
            @Valid @RequestBody DisponibilidadReservaRequest request) {

        reservaService.validarDisponibilidad(
                request.idSalon(),
                request.fechaEvento(),
                request.horaInicio(),
                request.horaFin());

        return ResponseEntity.ok(
                new MensajeResponse("Salon disponible"));
    }

    @PostMapping("/validar-monto-minimo")
    public ResponseEntity<MensajeResponse> validarMontoMinimo(
            @Valid @RequestBody MontoMinimoRequest request) {

        reservaService.validarMontoMinimo(
                request.montoTotal());

        return ResponseEntity.ok(
                new MensajeResponse("Monto minimo valido"));
    }

    @PostMapping("/calcular-monto-total")
    public ResponseEntity<MontoResponse> calcularMontoTotal(
            @Valid @RequestBody CalculoMontoTotalRequest request) {

        BigDecimal resultado =
                reservaService.calcularMontoTotal(
                        request.montoBase(),
                        request.montoServicios());

        return ResponseEntity.ok(
                new MontoResponse(resultado));
    }

    @PostMapping("/calcular-deposito")
    public ResponseEntity<MontoResponse> calcularDeposito(
            @Valid @RequestBody CalculoDepositoRequest request) {

        BigDecimal resultado =
                reservaService.calcularMontoDeposito(
                        request.montoTotal());

        return ResponseEntity.ok(
                new MontoResponse(resultado));
    }

    @PostMapping("/validar-transicion")
    public ResponseEntity<MensajeResponse> validarTransicion(
            @Valid @RequestBody TransicionReservaRequest request) {

        reservaService.validarTransicionEstado(
                request.estadoActual(),
                request.nuevoEstado());

        return ResponseEntity.ok(
                new MensajeResponse(
                        "Transicion de reserva valida"));
    }
}