package com.bembosevents.controller;

import com.bembosevents.dto.sede.SedeResumenResponse;
import com.bembosevents.service.SedeBackofficeService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/backoffice/sedes")
@PreAuthorize(
        "hasAnyRole('ADMINISTRADOR','ENCARGADO_LOCAL')"
)
public class SedeBackofficeController {

    private final SedeBackofficeService sedeBackofficeService;

    public SedeBackofficeController(
            SedeBackofficeService sedeBackofficeService) {

        this.sedeBackofficeService =
                sedeBackofficeService;
    }

    @GetMapping
    public List<SedeResumenResponse> listar(
            Authentication authentication) {

        return sedeBackofficeService
                .listarAccesibles(authentication);
    }

    @GetMapping("/{idSede}")
    public SedeResumenResponse obtener(
            @PathVariable Integer idSede,
            Authentication authentication) {

        return sedeBackofficeService
                .obtenerAccesible(
                        idSede,
                        authentication
                );
    }
}