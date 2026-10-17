package com.bembosevents.controller;

import com.bembosevents.dto.pago.CrearPagoRequest;
import com.bembosevents.dto.pago.PagoResponse;
import com.bembosevents.service.PagoService;
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
@RequestMapping(
        "/api/reservas/{idReserva}/pagos"
)
@PreAuthorize(
        "hasRole('CLIENTE')"
)
public class PagoClienteController {

    private final PagoService pagoService;

    public PagoClienteController(
            PagoService pagoService) {

        this.pagoService =
                pagoService;
    }

    @PostMapping
    public ResponseEntity<PagoResponse> iniciarPago(
            @PathVariable Integer idReserva,
            @Valid @RequestBody CrearPagoRequest request,
            Authentication authentication) {

        PagoResponse response =
                pagoService.iniciarPago(
                        authentication.getName(),
                        idReserva,
                        request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<PagoResponse>> listarPagos(
            @PathVariable Integer idReserva,
            Authentication authentication) {

        List<PagoResponse> response =
                pagoService.listarPagosPropios(
                        authentication.getName(),
                        idReserva);

        return ResponseEntity.ok(
                response);
    }
}