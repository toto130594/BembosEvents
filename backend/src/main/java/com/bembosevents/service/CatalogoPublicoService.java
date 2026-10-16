package com.bembosevents.service;

import com.bembosevents.dto.catalogo.HorarioSalonResponse;
import com.bembosevents.dto.catalogo.PaqueteResponse;
import com.bembosevents.dto.catalogo.SalonPublicoResponse;
import com.bembosevents.dto.catalogo.SedePublicaResponse;
import com.bembosevents.dto.catalogo.ServicioAdicionalResponse;
import com.bembosevents.dto.catalogo.TematicaResponse;

import java.util.List;

public interface CatalogoPublicoService {

    List<SedePublicaResponse> listarSedes();

    SedePublicaResponse obtenerSede(
            Integer idSede);

    List<SalonPublicoResponse> listarSalonesPorSede(
            Integer idSede);

    SalonPublicoResponse obtenerSalon(
            Integer idSalon);

    List<HorarioSalonResponse> listarHorariosSalon(
            Integer idSalon);

    List<PaqueteResponse> listarPaquetes();

    PaqueteResponse obtenerPaquete(
            Integer idPaquete);

    List<TematicaResponse> listarTematicas();

    TematicaResponse obtenerTematica(
            Integer idTematica);

    List<ServicioAdicionalResponse> listarServiciosAdicionales();

    ServicioAdicionalResponse obtenerServicioAdicional(
            Integer idServicioAdicional);
}
