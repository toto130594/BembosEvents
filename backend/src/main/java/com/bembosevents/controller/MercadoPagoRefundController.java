package com.bembosevents.controller;

import com.bembosevents.dto.pago.PagoRefundResponse;
import com.bembosevents.service.MercadoPagoRefundService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
        "/api/backoffice/pagos")
@PreAuthorize(
        "hasRole('ADMINISTRADOR')")
public class MercadoPagoRefundController {

    private final MercadoPagoRefundService refundService;

    public MercadoPagoRefundController(
            MercadoPagoRefundService refundService) {

        this.refundService =
                refundService;
    }

    @PostMapping(
            "/{idPago}/reembolso")
    public ResponseEntity<PagoRefundResponse> reembolsarTotal(
            @PathVariable Integer idPago) {

        return ResponseEntity.ok(
                refundService
                        .reembolsarTotal(
                                idPago));
    }
}