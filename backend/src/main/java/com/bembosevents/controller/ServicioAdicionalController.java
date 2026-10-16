package com.bembosevents.controller;

import com.bembosevents.dto.catalogo.ServicioAdicionalResponse;
import com.bembosevents.service.CatalogoPublicoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/servicios-adicionales")
public class ServicioAdicionalController {

    private final CatalogoPublicoService catalogoPublicoService;

    public ServicioAdicionalController(
            CatalogoPublicoService catalogoPublicoService) {

        this.catalogoPublicoService = catalogoPublicoService;
    }

    @GetMapping
    public List<ServicioAdicionalResponse> listar() {

        return catalogoPublicoService
                .listarServiciosAdicionales();
    }

    @GetMapping("/{idServicioAdicional}")
    public ServicioAdicionalResponse obtener(
            @PathVariable Integer idServicioAdicional) {

        return catalogoPublicoService
                .obtenerServicioAdicional(
                        idServicioAdicional);
    }
}
