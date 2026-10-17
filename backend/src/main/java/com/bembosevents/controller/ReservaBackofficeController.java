package com.bembosevents.controller;

import com.bembosevents.dto.reserva.CambiarEstadoReservaRequest;
import com.bembosevents.dto.reserva.ReservaDetalleResponse;
import com.bembosevents.dto.reserva.ReservaResumenResponse;
import com.bembosevents.service.ReservaBackofficeService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/backoffice/reservas")
@PreAuthorize(
        "hasAnyRole('ADMINISTRADOR','ENCARGADO_LOCAL')"
)
public class ReservaBackofficeController {

    private final ReservaBackofficeService reservaBackofficeService;

    public ReservaBackofficeController(
            ReservaBackofficeService reservaBackofficeService) {

        this.reservaBackofficeService =
                reservaBackofficeService;
    }

    @GetMapping
    public List<ReservaResumenResponse> listar(
            Authentication authentication) {

        return reservaBackofficeService
                .listarAccesibles(authentication);
    }

    @GetMapping("/{idReserva}")
    public ReservaDetalleResponse obtener(
            @PathVariable Integer idReserva,
            Authentication authentication) {

        return reservaBackofficeService
                .obtenerAccesible(
                        idReserva,
                        authentication);
    }

    @PatchMapping("/{idReserva}/estado")
    public ReservaDetalleResponse cambiarEstado(
            @PathVariable Integer idReserva,
            @Valid
            @RequestBody
            CambiarEstadoReservaRequest request,
            Authentication authentication) {

        return reservaBackofficeService
                .cambiarEstado(
                        idReserva,
                        request,
                        authentication);
    }
}