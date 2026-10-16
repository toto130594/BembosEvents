package com.bembosevents.controller;

import com.bembosevents.dto.catalogo.DisponibilidadSalonResponse;
import com.bembosevents.dto.catalogo.HorarioSalonResponse;
import com.bembosevents.dto.catalogo.SalonPublicoResponse;
import com.bembosevents.service.CatalogoPublicoService;
import com.bembosevents.service.DisponibilidadSalonService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/salones")
public class SalonController {

    private final CatalogoPublicoService catalogoPublicoService;
    private final DisponibilidadSalonService disponibilidadSalonService;

    public SalonController(
            CatalogoPublicoService catalogoPublicoService,
            DisponibilidadSalonService disponibilidadSalonService) {

        this.catalogoPublicoService = catalogoPublicoService;
        this.disponibilidadSalonService = disponibilidadSalonService;
    }

    @GetMapping("/{idSalon}")
    public SalonPublicoResponse obtener(
            @PathVariable Integer idSalon) {

        return catalogoPublicoService.obtenerSalon(idSalon);
    }

    @GetMapping("/{idSalon}/horarios")
    public List<HorarioSalonResponse> listarHorarios(
            @PathVariable Integer idSalon) {

        return catalogoPublicoService
                .listarHorariosSalon(idSalon);
    }

    @GetMapping("/{idSalon}/disponibilidad")
    public DisponibilidadSalonResponse consultarDisponibilidad(
            @PathVariable Integer idSalon,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fecha,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
            LocalTime horaInicio,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
            LocalTime horaFin) {

        return disponibilidadSalonService.consultar(
                idSalon,
                fecha,
                horaInicio,
                horaFin);
    }
}
