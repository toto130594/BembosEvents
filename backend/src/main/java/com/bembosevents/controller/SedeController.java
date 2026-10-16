package com.bembosevents.controller;

import com.bembosevents.dto.catalogo.SalonPublicoResponse;
import com.bembosevents.dto.catalogo.SedePublicaResponse;
import com.bembosevents.service.CatalogoPublicoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sedes")
public class SedeController {

    private final CatalogoPublicoService catalogoPublicoService;

    public SedeController(
            CatalogoPublicoService catalogoPublicoService) {

        this.catalogoPublicoService = catalogoPublicoService;
    }

    @GetMapping
    public List<SedePublicaResponse> listar() {

        return catalogoPublicoService.listarSedes();
    }

    @GetMapping("/{idSede}")
    public SedePublicaResponse obtener(
            @PathVariable Integer idSede) {

        return catalogoPublicoService.obtenerSede(idSede);
    }

    @GetMapping("/{idSede}/salones")
    public List<SalonPublicoResponse> listarSalones(
            @PathVariable Integer idSede) {

        return catalogoPublicoService
                .listarSalonesPorSede(idSede);
    }
}
