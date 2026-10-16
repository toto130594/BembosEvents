package com.bembosevents.service.impl;

import com.bembosevents.dto.catalogo.SedePublicaResponse;
import com.bembosevents.entity.Salon;
import com.bembosevents.entity.Sede;
import com.bembosevents.exception.RecursoNoEncontradoException;
import com.bembosevents.repository.HorarioSalonRepository;
import com.bembosevents.repository.PaqueteRepository;
import com.bembosevents.repository.SalonRepository;
import com.bembosevents.repository.SedeRepository;
import com.bembosevents.repository.ServicioAdicionalRepository;
import com.bembosevents.repository.TematicaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogoPublicoServiceImplTest {

    @Mock
    private SedeRepository sedeRepository;

    @Mock
    private SalonRepository salonRepository;

    @Mock
    private HorarioSalonRepository horarioSalonRepository;

    @Mock
    private PaqueteRepository paqueteRepository;

    @Mock
    private TematicaRepository tematicaRepository;

    @Mock
    private ServicioAdicionalRepository servicioAdicionalRepository;

    private CatalogoPublicoServiceImpl service;

    @BeforeEach
    void setUp() {

        service = new CatalogoPublicoServiceImpl(
                sedeRepository,
                salonRepository,
                horarioSalonRepository,
                paqueteRepository,
                tematicaRepository,
                servicioAdicionalRepository);
    }

    @Test
    void debeListarSedesActivas() {

        Sede sede = crearSede(
                1,
                "San Miguel",
                true);

        when(sedeRepository
                .findByActivoTrueOrderByNombreAsc())
                .thenReturn(List.of(sede));

        List<SedePublicaResponse> resultado =
                service.listarSedes();

        assertEquals(1, resultado.size());
        assertEquals(1, resultado.get(0).idSede());
        assertEquals("San Miguel", resultado.get(0).nombre());
    }

    @Test
    void debeResponderNoEncontradoParaSedeNoDisponible() {

        when(sedeRepository
                .findByIdSedeAndActivoTrue(99))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> service.obtenerSede(99));
    }

    @Test
    void debeListarSoloSalonesActivosDeSedeActiva() {

        Sede sede = crearSede(
                1,
                "San Miguel",
                true);

        Salon salon = new Salon();
        salon.setIdSalon(10);
        salon.setSede(sede);
        salon.setNombre("Salon Principal");
        salon.setDescripcion("Salon de prueba");
        salon.setCapacidad(50);
        salon.setActivo(true);

        when(sedeRepository
                .findByIdSedeAndActivoTrue(1))
                .thenReturn(Optional.of(sede));

        when(salonRepository
                .findBySede_IdSedeAndActivoTrueOrderByNombreAsc(1))
                .thenReturn(List.of(salon));

        var resultado =
                service.listarSalonesPorSede(1);

        assertEquals(1, resultado.size());
        assertEquals(10, resultado.get(0).idSalon());
        assertEquals(1, resultado.get(0).idSede());
    }

    @Test
    void debeOcultarSalonSiSuSedeEstaInactiva() {

        Sede sede = crearSede(
                1,
                "San Miguel",
                false);

        Salon salon = new Salon();
        salon.setIdSalon(10);
        salon.setSede(sede);
        salon.setActivo(true);

        when(salonRepository
                .findByIdSalonAndActivoTrue(10))
                .thenReturn(Optional.of(salon));

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> service.obtenerSalon(10));
    }

    private Sede crearSede(
            Integer id,
            String nombre,
            boolean activo) {

        Sede sede = new Sede();
        sede.setIdSede(id);
        sede.setNombre(nombre);
        sede.setDireccion("Direccion");
        sede.setDistrito("Lima");
        sede.setTelefono("999999999");
        sede.setActivo(activo);

        return sede;
    }
}
