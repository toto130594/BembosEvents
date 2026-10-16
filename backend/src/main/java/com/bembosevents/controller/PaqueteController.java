package com.bembosevents.controller;

import com.bembosevents.dto.catalogo.PaqueteResponse;
import com.bembosevents.service.CatalogoPublicoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/paquetes")
public class PaqueteController {

    private final CatalogoPublicoService catalogoPublicoService;

    public PaqueteController(
            CatalogoPublicoService catalogoPublicoService) {

        this.catalogoPublicoService = catalogoPublicoService;
    }

    @GetMapping
    public List<PaqueteResponse> listar() {

        return catalogoPublicoService.listarPaquetes();
    }

    @GetMapping("/{idPaquete}")
    public PaqueteResponse obtener(
            @PathVariable Integer idPaquete) {

        return catalogoPublicoService
                .obtenerPaquete(idPaquete);
    }
}
