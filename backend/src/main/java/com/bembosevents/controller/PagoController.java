package com.bembosevents.controller;

import com.bembosevents.dto.common.MensajeResponse;
import com.bembosevents.dto.pago.TransicionPagoRequest;
import com.bembosevents.service.PagoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pagos")
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    @PostMapping("/validar-transicion")
    public ResponseEntity<MensajeResponse> validarTransicion(
            @Valid @RequestBody TransicionPagoRequest request) {

        pagoService.validarTransicionEstado(
                request.estadoActual(),
                request.nuevoEstado());

        return ResponseEntity.ok(
                new MensajeResponse(
                        "Transicion de pago valida"));
    }
}