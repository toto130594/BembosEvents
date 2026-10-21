package com.bembosevents.controller;

import com.bembosevents.dto.pago.PagoReconciliationItemResponse;
import com.bembosevents.dto.pago.PagoReconciliationResponse;
import com.bembosevents.service.MercadoPagoReconciliationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
        "/api/backoffice/pagos/mercado-pago")
@PreAuthorize(
        "hasRole('ADMINISTRADOR')")
public class MercadoPagoReconciliationController {

    private final MercadoPagoReconciliationService reconciliationService;

    public MercadoPagoReconciliationController(
            MercadoPagoReconciliationService reconciliationService) {

        this.reconciliationService =
                reconciliationService;
    }

    @PostMapping(
            "/{idPago}/reconciliar")
    public ResponseEntity<PagoReconciliationItemResponse> reconciliarPago(
            @PathVariable Integer idPago) {

        return ResponseEntity.ok(
                reconciliationService
                        .reconciliarPago(
                                idPago));
    }

    @PostMapping(
            "/reconciliar")
    public ResponseEntity<PagoReconciliationResponse> reconciliarLote() {

        return ResponseEntity.ok(
                reconciliationService
                        .reconciliarLote());
    }
}