package com.bembosevents.controller;

import com.bembosevents.dto.catalogo.TematicaResponse;
import com.bembosevents.service.CatalogoPublicoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tematicas")
public class TematicaController {

    private final CatalogoPublicoService catalogoPublicoService;

    public TematicaController(
            CatalogoPublicoService catalogoPublicoService) {

        this.catalogoPublicoService = catalogoPublicoService;
    }

    @GetMapping
    public List<TematicaResponse> listar() {

        return catalogoPublicoService.listarTematicas();
    }

    @GetMapping("/{idTematica}")
    public TematicaResponse obtener(
            @PathVariable Integer idTematica) {

        return catalogoPublicoService
                .obtenerTematica(idTematica);
    }
}
