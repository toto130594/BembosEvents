package com.bembosevents.controller;

import com.bembosevents.dto.pago.PagoResponse;
import com.bembosevents.service.PagoBackofficeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/backoffice/pagos")
@PreAuthorize(
        "hasAnyRole('ADMINISTRADOR','ENCARGADO_LOCAL')"
)
public class PagoBackofficeController {

    private final PagoBackofficeService pagoBackofficeService;

    public PagoBackofficeController(
            PagoBackofficeService pagoBackofficeService) {

        this.pagoBackofficeService =
                pagoBackofficeService;
    }

    @GetMapping
    public ResponseEntity<List<PagoResponse>> listarPagos(
            Authentication authentication) {

        return ResponseEntity.ok(
                pagoBackofficeService.listarAccesibles(
                        authentication));
    }

    @GetMapping("/{idPago}")
    public ResponseEntity<PagoResponse> obtenerPago(
            @PathVariable Integer idPago,
            Authentication authentication) {

        return ResponseEntity.ok(
                pagoBackofficeService.obtenerAccesible(
                        idPago,
                        authentication));
    }
}