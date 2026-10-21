package com.bembosevents.controller;

import com.bembosevents.dto.pago.IniciarPagoCheckoutRequest;
import com.bembosevents.dto.pago.PagoCheckoutResponse;
import com.bembosevents.service.PagoCheckoutService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
        "/api/reservas/{idReserva}/pagos/checkout")
@PreAuthorize("hasRole('CLIENTE')")
public class PagoCheckoutController {

    private final PagoCheckoutService pagoCheckoutService;

    public PagoCheckoutController(
            PagoCheckoutService pagoCheckoutService) {

        this.pagoCheckoutService =
                pagoCheckoutService;
    }

    @PostMapping
    public ResponseEntity<PagoCheckoutResponse> iniciarCheckout(
            @PathVariable Integer idReserva,
            @Valid @RequestBody IniciarPagoCheckoutRequest request,
            Authentication authentication) {

        PagoCheckoutResponse response =
                pagoCheckoutService.iniciarCheckout(
                        authentication.getName(),
                        idReserva,
                        request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}