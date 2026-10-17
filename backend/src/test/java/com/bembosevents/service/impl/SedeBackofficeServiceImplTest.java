package com.bembosevents.service.impl;

import com.bembosevents.dto.sede.SedeResumenResponse;
import com.bembosevents.entity.Sede;
import com.bembosevents.exception.RecursoNoEncontradoException;
import com.bembosevents.repository.SedeRepository;
import com.bembosevents.service.SedeAccessService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SedeBackofficeServiceImplTest {

    @Mock
    private SedeRepository sedeRepository;

    @Mock
    private SedeAccessService sedeAccessService;

    private SedeBackofficeServiceImpl service;

    @BeforeEach
    void setUp() {

        service =
                new SedeBackofficeServiceImpl(
                        sedeRepository,
                        sedeAccessService);
    }

    @Test
    void administradorListaSoloSedesActivas() {

        Authentication authentication =
                autenticacion(
                        "admin@bembos.test",
                        "ROLE_ADMINISTRADOR");

        Sede activa =
                sede(
                        1,
                        true);

        Sede inactiva =
                sede(
                        2,
                        false);

        when(
                sedeAccessService
                        .esAdministrador(authentication)
        ).thenReturn(true);

        when(
                sedeRepository.findAll()
        ).thenReturn(
                List.of(
                        activa,
                        inactiva));

        List<SedeResumenResponse> resultado =
                service.listarAccesibles(
                        authentication);

        assertEquals(
                1,
                resultado.size());

        assertEquals(
                1,
                resultado.get(0).idSede());

        verify(
                sedeRepository
        ).findAll();

        verify(
                sedeAccessService,
                never()
        ).obtenerSedeAsignada(
                anyString());
    }

    @Test
    void encargadoListaSoloSuSedeAsignada() {

        Authentication authentication =
                autenticacion(
                        "encargado@bembos.test",
                        "ROLE_ENCARGADO_LOCAL");

        Sede sede =
                sede(
                        3,
                        true);

        when(
                sedeAccessService
                        .esAdministrador(authentication)
        ).thenReturn(false);

        when(
                sedeAccessService
                        .esEncargadoLocal(authentication)
        ).thenReturn(true);

        when(
                sedeAccessService
                        .obtenerSedeAsignada(
                                "encargado@bembos.test")
        ).thenReturn(3);

        when(
                sedeRepository
                        .findByIdSedeAndActivoTrue(3)
        ).thenReturn(
                Optional.of(sede));

        List<SedeResumenResponse> resultado =
                service.listarAccesibles(
                        authentication);

        assertEquals(
                1,
                resultado.size());

        assertEquals(
                3,
                resultado.get(0).idSede());
    }

    @Test
    void administradorPuedeConsultarSedeActiva() {

        Authentication authentication =
                autenticacion(
                        "admin@bembos.test",
                        "ROLE_ADMINISTRADOR");

        Sede sede =
                sede(
                        4,
                        true);

        when(
                sedeAccessService
                        .esAdministrador(authentication)
        ).thenReturn(true);

        when(
                sedeRepository
                        .findByIdSedeAndActivoTrue(4)
        ).thenReturn(
                Optional.of(sede));

        SedeResumenResponse resultado =
                service.obtenerAccesible(
                        4,
                        authentication);

        assertEquals(
                4,
                resultado.idSede());
    }

    @Test
    void administradorSedeInexistenteResponde404() {

        Authentication authentication =
                autenticacion(
                        "admin@bembos.test",
                        "ROLE_ADMINISTRADOR");

        when(
                sedeAccessService
                        .esAdministrador(authentication)
        ).thenReturn(true);

        when(
                sedeRepository
                        .findByIdSedeAndActivoTrue(99)
        ).thenReturn(
                Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () ->
                        service.obtenerAccesible(
                                99,
                                authentication));
    }

    @Test
    void encargadoPuedeConsultarSuSede() {

        Authentication authentication =
                autenticacion(
                        "encargado@bembos.test",
                        "ROLE_ENCARGADO_LOCAL");

        Sede sede =
                sede(
                        3,
                        true);

        when(
                sedeAccessService
                        .esAdministrador(authentication)
        ).thenReturn(false);

        when(
                sedeAccessService
                        .esEncargadoLocal(authentication)
        ).thenReturn(true);

        when(
                sedeAccessService
                        .obtenerSedeAsignada(
                                "encargado@bembos.test")
        ).thenReturn(3);

        when(
                sedeRepository
                        .findByIdSedeAndActivoTrue(3)
        ).thenReturn(
                Optional.of(sede));

        SedeResumenResponse resultado =
                service.obtenerAccesible(
                        3,
                        authentication);

        assertEquals(
                3,
                resultado.idSede());
    }

    @Test
    void encargadoNoPuedeConsultarOtraSedeYResponde404() {

        Authentication authentication =
                autenticacion(
                        "encargado@bembos.test",
                        "ROLE_ENCARGADO_LOCAL");

        when(
                sedeAccessService
                        .esAdministrador(authentication)
        ).thenReturn(false);

        when(
                sedeAccessService
                        .esEncargadoLocal(authentication)
        ).thenReturn(true);

        when(
                sedeAccessService
                        .obtenerSedeAsignada(
                                "encargado@bembos.test")
        ).thenReturn(3);

        RecursoNoEncontradoException exception =
                assertThrows(
                        RecursoNoEncontradoException.class,
                        () ->
                                service.obtenerAccesible(
                                        5,
                                        authentication));

        assertEquals(
                "Sede no encontrada",
                exception.getMessage());

        verifyNoInteractions(
                sedeRepository);
    }

    @Test
    void usuarioSinRolBackofficeNoPuedeConsultarSede() {

        Authentication authentication =
                autenticacion(
                        "cliente@bembos.test",
                        "ROLE_CLIENTE");

        when(
                sedeAccessService
                        .esAdministrador(authentication)
        ).thenReturn(false);

        when(
                sedeAccessService
                        .esEncargadoLocal(authentication)
        ).thenReturn(false);

        assertThrows(
                AccessDeniedException.class,
                () ->
                        service.obtenerAccesible(
                                1,
                                authentication));

        verifyNoInteractions(
                sedeRepository);
    }

    @Test
    void idSedeDebeSerPositivo() {

        Authentication authentication =
                autenticacion(
                        "admin@bembos.test",
                        "ROLE_ADMINISTRADOR");

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.obtenerAccesible(
                                0,
                                authentication));

        verifyNoInteractions(
                sedeRepository,
                sedeAccessService);
    }

    private Authentication autenticacion(
            String correo,
            String rol) {

        return new UsernamePasswordAuthenticationToken(
                correo,
                null,
                List.of(
                        new SimpleGrantedAuthority(
                                rol)));
    }

    private Sede sede(
            Integer id,
            boolean activo) {

        Sede sede =
                new Sede();

        sede.setIdSede(id);
        sede.setNombre(
                "Sede " + id);
        sede.setDireccion(
                "Direccion " + id);
        sede.setDistrito(
                "Distrito " + id);
        sede.setTelefono(
                "999999999");
        sede.setActivo(activo);

        return sede;
    }
}
