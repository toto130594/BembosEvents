package com.bembosevents.controller;

import com.bembosevents.ai.dashboard.AiBackofficeDashboardResponse;
import com.bembosevents.ai.dashboard.AiBackofficeDashboardService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/backoffice/ai/dashboard")
@PreAuthorize(
        "hasAnyRole('ADMINISTRADOR','ENCARGADO_LOCAL')")
public class AiBackofficeDashboardController {

    private final AiBackofficeDashboardService dashboardService;

    public AiBackofficeDashboardController(
            AiBackofficeDashboardService dashboardService) {

        this.dashboardService =
                dashboardService;
    }

    @GetMapping
    public ResponseEntity<AiBackofficeDashboardResponse> obtener(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate desde,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate hasta,
            @RequestParam(required = false)
            Integer idSede,
            Authentication authentication) {

        return ResponseEntity.ok(
                dashboardService.obtener(
                        desde,
                        hasta,
                        idSede,
                        authentication));
    }
}