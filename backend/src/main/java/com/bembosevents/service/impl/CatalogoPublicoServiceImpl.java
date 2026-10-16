package com.bembosevents.service.impl;

import com.bembosevents.dto.catalogo.HorarioSalonResponse;
import com.bembosevents.dto.catalogo.PaqueteResponse;
import com.bembosevents.dto.catalogo.SalonPublicoResponse;
import com.bembosevents.dto.catalogo.SedePublicaResponse;
import com.bembosevents.dto.catalogo.ServicioAdicionalResponse;
import com.bembosevents.dto.catalogo.TematicaResponse;
import com.bembosevents.entity.HorarioSalon;
import com.bembosevents.entity.Paquete;
import com.bembosevents.entity.Salon;
import com.bembosevents.entity.Sede;
import com.bembosevents.entity.ServicioAdicional;
import com.bembosevents.entity.Tematica;
import com.bembosevents.exception.RecursoNoEncontradoException;
import com.bembosevents.repository.HorarioSalonRepository;
import com.bembosevents.repository.PaqueteRepository;
import com.bembosevents.repository.SalonRepository;
import com.bembosevents.repository.SedeRepository;
import com.bembosevents.repository.ServicioAdicionalRepository;
import com.bembosevents.repository.TematicaRepository;
import com.bembosevents.service.CatalogoPublicoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class CatalogoPublicoServiceImpl
        implements CatalogoPublicoService {

    private final SedeRepository sedeRepository;
    private final SalonRepository salonRepository;
    private final HorarioSalonRepository horarioSalonRepository;
    private final PaqueteRepository paqueteRepository;
    private final TematicaRepository tematicaRepository;
    private final ServicioAdicionalRepository servicioAdicionalRepository;

    public CatalogoPublicoServiceImpl(
            SedeRepository sedeRepository,
            SalonRepository salonRepository,
            HorarioSalonRepository horarioSalonRepository,
            PaqueteRepository paqueteRepository,
            TematicaRepository tematicaRepository,
            ServicioAdicionalRepository servicioAdicionalRepository) {

        this.sedeRepository = sedeRepository;
        this.salonRepository = salonRepository;
        this.horarioSalonRepository = horarioSalonRepository;
        this.paqueteRepository = paqueteRepository;
        this.tematicaRepository = tematicaRepository;
        this.servicioAdicionalRepository = servicioAdicionalRepository;
    }

    @Override
    public List<SedePublicaResponse> listarSedes() {

        return sedeRepository
                .findByActivoTrueOrderByNombreAsc()
                .stream()
                .map(this::mapSede)
                .toList();
    }

    @Override
    public SedePublicaResponse obtenerSede(
            Integer idSede) {

        return mapSede(
                buscarSedeActiva(idSede));
    }

    @Override
    public List<SalonPublicoResponse> listarSalonesPorSede(
            Integer idSede) {

        buscarSedeActiva(idSede);

        return salonRepository
                .findBySede_IdSedeAndActivoTrueOrderByNombreAsc(
                        idSede)
                .stream()
                .map(this::mapSalon)
                .toList();
    }

    @Override
    public SalonPublicoResponse obtenerSalon(
            Integer idSalon) {

        return mapSalon(
                buscarSalonPublico(idSalon));
    }

    @Override
    public List<HorarioSalonResponse> listarHorariosSalon(
            Integer idSalon) {

        buscarSalonPublico(idSalon);

        return horarioSalonRepository
                .findBySalon_IdSalonAndActivoTrue(idSalon)
                .stream()
                .sorted(
                        Comparator
                                .comparingInt(
                                        (HorarioSalon horario) ->
                                                ordenDia(
                                                        horario.getDiaSemana()))
                                .thenComparing(
                                        HorarioSalon::getHoraInicio))
                .map(this::mapHorario)
                .toList();
    }

    @Override
    public List<PaqueteResponse> listarPaquetes() {

        return paqueteRepository
                .findByActivoTrueOrderByNombreAsc()
                .stream()
                .map(this::mapPaquete)
                .toList();
    }

    @Override
    public PaqueteResponse obtenerPaquete(
            Integer idPaquete) {

        Paquete paquete = paqueteRepository
                .findByIdPaqueteAndActivoTrue(idPaquete)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Paquete no encontrado"));

        return mapPaquete(paquete);
    }

    @Override
    public List<TematicaResponse> listarTematicas() {

        return tematicaRepository
                .findByActivoTrueOrderByNombreAsc()
                .stream()
                .map(this::mapTematica)
                .toList();
    }

    @Override
    public TematicaResponse obtenerTematica(
            Integer idTematica) {

        Tematica tematica = tematicaRepository
                .findByIdTematicaAndActivoTrue(idTematica)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Tematica no encontrada"));

        return mapTematica(tematica);
    }

    @Override
    public List<ServicioAdicionalResponse> listarServiciosAdicionales() {

        return servicioAdicionalRepository
                .findByActivoTrueOrderByNombreAsc()
                .stream()
                .map(this::mapServicio)
                .toList();
    }

    @Override
    public ServicioAdicionalResponse obtenerServicioAdicional(
            Integer idServicioAdicional) {

        ServicioAdicional servicio = servicioAdicionalRepository
                .findByIdServicioAdicionalAndActivoTrue(
                        idServicioAdicional)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Servicio adicional no encontrado"));

        return mapServicio(servicio);
    }

    private Sede buscarSedeActiva(
            Integer idSede) {

        if (idSede == null) {
            throw new IllegalArgumentException(
                    "El id de sede es obligatorio");
        }

        return sedeRepository
                .findByIdSedeAndActivoTrue(idSede)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Sede no encontrada"));
    }

    private Salon buscarSalonPublico(
            Integer idSalon) {

        if (idSalon == null) {
            throw new IllegalArgumentException(
                    "El id de salon es obligatorio");
        }

        Salon salon = salonRepository
                .findByIdSalonAndActivoTrue(idSalon)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Salon no encontrado"));

        if (salon.getSede() == null
                || !Boolean.TRUE.equals(
                        salon.getSede().getActivo())) {

            throw new RecursoNoEncontradoException(
                    "Salon no encontrado");
        }

        return salon;
    }

    private SedePublicaResponse mapSede(
            Sede sede) {

        return new SedePublicaResponse(
                sede.getIdSede(),
                sede.getNombre(),
                sede.getDireccion(),
                sede.getDistrito(),
                sede.getTelefono());
    }

    private SalonPublicoResponse mapSalon(
            Salon salon) {

        return new SalonPublicoResponse(
                salon.getIdSalon(),
                salon.getSede().getIdSede(),
                salon.getNombre(),
                salon.getDescripcion(),
                salon.getCapacidad());
    }

    private HorarioSalonResponse mapHorario(
            HorarioSalon horario) {

        return new HorarioSalonResponse(
                horario.getIdHorarioSalon(),
                horario.getSalon().getIdSalon(),
                horario.getDiaSemana(),
                horario.getHoraInicio(),
                horario.getHoraFin());
    }

    private PaqueteResponse mapPaquete(
            Paquete paquete) {

        return new PaqueteResponse(
                paquete.getIdPaquete(),
                paquete.getNombre(),
                paquete.getDescripcion(),
                paquete.getPrecioBase(),
                paquete.getCantidadPersonasBase());
    }

    private TematicaResponse mapTematica(
            Tematica tematica) {

        return new TematicaResponse(
                tematica.getIdTematica(),
                tematica.getNombre(),
                tematica.getDescripcion(),
                tematica.getPrecioAdicional());
    }

    private ServicioAdicionalResponse mapServicio(
            ServicioAdicional servicio) {

        return new ServicioAdicionalResponse(
                servicio.getIdServicioAdicional(),
                servicio.getNombre(),
                servicio.getDescripcion(),
                servicio.getPrecio());
    }

    private int ordenDia(
            String diaSemana) {

        if (diaSemana == null) {
            return 99;
        }

        return switch (
                diaSemana.strip().toUpperCase(Locale.ROOT)) {

            case "LUNES" -> 1;
            case "MARTES" -> 2;
            case "MIERCOLES", "MIÉRCOLES" -> 3;
            case "JUEVES" -> 4;
            case "VIERNES" -> 5;
            case "SABADO", "SÁBADO" -> 6;
            case "DOMINGO" -> 7;
            default -> 99;
        };
    }
}
