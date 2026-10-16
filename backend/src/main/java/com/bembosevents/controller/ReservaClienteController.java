package com.bembosevents.controller;

import com.bembosevents.dto.reserva.CrearReservaRequest;
import com.bembosevents.dto.reserva.ReservaCreadaResponse;
import com.bembosevents.service.ReservaClienteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
