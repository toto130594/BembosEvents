package com.bembosevents.controller;

import com.bembosevents.dto.pago.PagoResponse;
import com.bembosevents.service.PagoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pagos")
@PreAuthorize("hasRole('CLIENTE')")
public class PagoConsultaClienteController {

    private final PagoService pagoService;

    public PagoConsultaClienteController(
            PagoService pagoService) {

        this.pagoService =
                pagoService;
    }

    @GetMapping("/{idPago}")
    public ResponseEntity<PagoResponse> obtenerPago(
            @PathVariable Integer idPago,
            Authentication authentication) {

        PagoResponse response =
                pagoService.obtenerPagoPropio(
                        authentication.getName(),
                        idPago);

        return ResponseEntity.ok(
                response);
    }
}