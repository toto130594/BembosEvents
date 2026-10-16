package com.bembosevents.controller;

import com.bembosevents.dto.reserva.CrearReservaRequest;
import com.bembosevents.dto.reserva.ReservaCreadaResponse;
import com.bembosevents.dto.reserva.ReservaDetalleResponse;
import com.bembosevents.dto.reserva.ReservaResumenResponse;
import com.bembosevents.service.ReservaClienteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reservas")
public class ReservaClienteController {

    private final ReservaClienteService reservaClienteService;

    public ReservaClienteController(
            ReservaClienteService reservaClienteService) {

        this.reservaClienteService = reservaClienteService;
    }

    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<ReservaCreadaResponse> crear(
            Authentication authentication,
            @Valid @RequestBody CrearReservaRequest request) {

        ReservaCreadaResponse response =
                reservaClienteService.crear(
                        authentication.getName(),
                        request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/mias")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<List<ReservaResumenResponse>> misReservas(
            Authentication authentication) {

        return ResponseEntity.ok(
                reservaClienteService.misReservas(
                        authentication.getName()));
    }

    @GetMapping("/{idReserva}")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<ReservaDetalleResponse> consultarPropia(
            Authentication authentication,
            @PathVariable Integer idReserva) {

        return ResponseEntity.ok(
                reservaClienteService.consultarPropia(
                        authentication.getName(),
                        idReserva));
    }
}
