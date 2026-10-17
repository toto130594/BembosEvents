package com.bembosevents.service.impl;

import com.bembosevents.domain.EstadoReserva;
import com.bembosevents.dto.reserva.CambiarEstadoReservaRequest;
import com.bembosevents.dto.reserva.ReservaDetalleResponse;
import com.bembosevents.dto.reserva.ReservaResumenResponse;
import com.bembosevents.entity.HistorialReserva;
import com.bembosevents.entity.Reserva;
import com.bembosevents.entity.Salon;
import com.bembosevents.entity.Sede;
import com.bembosevents.entity.Usuario;
import com.bembosevents.exception.ConflictoReservaException;
import com.bembosevents.exception.RecursoNoEncontradoException;
import com.bembosevents.repository.HistorialReservaRepository;
import com.bembosevents.repository.ReservaRepository;
import com.bembosevents.repository.UsuarioRepository;
import com.bembosevents.service.ReservaService;
import com.bembosevents.service.SedeAccessService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservaBackofficeServiceImplTest {

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private SedeAccessService sedeAccessService;

    @Mock
    private ReservaService reservaService;

    @Mock
    private HistorialReservaRepository historialReservaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private ReservaBackofficeServiceImpl reservaBackofficeService;

    @Test
    void administradorDebeListarTodasLasReservas() {

        Authentication authentication =
                autenticacion(
                        "admin@bembos.test",
                        "ROLE_ADMINISTRADOR");

        Reserva reciente =
                crearReserva(
                        102,
                        2,
                        "PENDIENTE");

        Reserva anterior =
                crearReserva(
                        101,
                        1,
                        "PENDIENTE");

        when(sedeAccessService
                .esAdministrador(authentication))
                .thenReturn(true);

        when(reservaRepository
                .findAllByOrderByFechaCreacionDesc())
                .thenReturn(
                        List.of(
                                reciente,
                                anterior));

        List<ReservaResumenResponse> resultado =
                reservaBackofficeService
                        .listarAccesibles(authentication);

        assertEquals(
                2,
                resultado.size());

        assertEquals(
                102,
                resultado.get(0).idReserva());

        assertEquals(
                2,
                resultado.get(0).idSede());

        verify(reservaRepository)
                .findAllByOrderByFechaCreacionDesc();

        verify(
                reservaRepository,
                never())
                .findBySede_IdSedeOrderByFechaCreacionDesc(
                        anyInt());

        verify(
                sedeAccessService,
                never())
                .obtenerSedeAsignada(
                        anyString());
    }

    @Test
    void encargadoDebeListarSoloReservasDeSuSede() {

        String correo =
                "encargado@bembos.test";

        Authentication authentication =
                autenticacion(
                        correo,
                        "ROLE_ENCARGADO_LOCAL");

        Reserva reserva =
                crearReserva(
                        201,
                        3,
                        "PENDIENTE");

        when(sedeAccessService
                .esAdministrador(authentication))
                .thenReturn(false);

        when(sedeAccessService
                .esEncargadoLocal(authentication))
                .thenReturn(true);

        when(sedeAccessService
                .obtenerSedeAsignada(correo))
                .thenReturn(3);

        when(reservaRepository
                .findBySede_IdSedeOrderByFechaCreacionDesc(3))
                .thenReturn(
                        List.of(reserva));

        List<ReservaResumenResponse> resultado =
                reservaBackofficeService
                        .listarAccesibles(authentication);

        assertEquals(
                1,
                resultado.size());

        assertEquals(
                3,
                resultado.get(0).idSede());

        verify(reservaRepository)
                .findBySede_IdSedeOrderByFechaCreacionDesc(3);

        verify(
                reservaRepository,
                never())
                .findAllByOrderByFechaCreacionDesc();
    }

    @Test
    void encargadoSinReservasDebeObtenerListaVacia() {

        String correo =
                "encargado@bembos.test";

        Authentication authentication =
                autenticacion(
                        correo,
                        "ROLE_ENCARGADO_LOCAL");

        when(sedeAccessService
                .esAdministrador(authentication))
                .thenReturn(false);

        when(sedeAccessService
                .esEncargadoLocal(authentication))
                .thenReturn(true);

        when(sedeAccessService
                .obtenerSedeAsignada(correo))
                .thenReturn(3);

        when(reservaRepository
                .findBySede_IdSedeOrderByFechaCreacionDesc(3))
                .thenReturn(List.of());

        assertTrue(
                reservaBackofficeService
                        .listarAccesibles(authentication)
                        .isEmpty());
    }

    @Test
    void usuarioSinRolBackofficeDebeSerRechazado() {

        Authentication authentication =
                autenticacion(
                        "cliente@bembos.test",
                        "ROLE_CLIENTE");

        when(sedeAccessService
                .esAdministrador(authentication))
                .thenReturn(false);

        when(sedeAccessService
                .esEncargadoLocal(authentication))
                .thenReturn(false);

        assertThrows(
                AccessDeniedException.class,
                () ->
                        reservaBackofficeService
                                .listarAccesibles(
                                        authentication));

        verifyNoInteractions(
                reservaRepository);
    }

    @Test
    void administradorDebeConsultarReservaDeCualquierSede() {

        Authentication authentication =
                autenticacion(
                        "admin@bembos.test",
                        "ROLE_ADMINISTRADOR");

        Reserva reserva =
                crearReserva(
                        301,
                        8,
                        "PENDIENTE");

        when(sedeAccessService
                .esAdministrador(authentication))
                .thenReturn(true);

        when(reservaRepository
                .findById(301))
                .thenReturn(
                        Optional.of(reserva));

        ReservaDetalleResponse resultado =
                reservaBackofficeService
                        .obtenerAccesible(
                                301,
                                authentication);

        assertEquals(
                301,
                resultado.idReserva());

        assertEquals(
                8,
                resultado.idSede());

        verify(reservaRepository)
                .findById(301);

        verify(
                reservaRepository,
                never())
                .findByIdReservaAndSede_IdSede(
                        anyInt(),
                        anyInt());
    }

    @Test
    void encargadoDebeConsultarReservaDeSuSede() {

        String correo =
                "encargado@bembos.test";

        Authentication authentication =
                autenticacion(
                        correo,
                        "ROLE_ENCARGADO_LOCAL");

        Reserva reserva =
                crearReserva(
                        302,
                        3,
                        "PENDIENTE");

        when(sedeAccessService
                .esAdministrador(authentication))
                .thenReturn(false);

        when(sedeAccessService
                .esEncargadoLocal(authentication))
                .thenReturn(true);

        when(sedeAccessService
                .obtenerSedeAsignada(correo))
                .thenReturn(3);

        when(reservaRepository
                .findByIdReservaAndSede_IdSede(
                        302,
                        3))
                .thenReturn(
                        Optional.of(reserva));

        ReservaDetalleResponse resultado =
                reservaBackofficeService
                        .obtenerAccesible(
                                302,
                                authentication);

        assertEquals(
                3,
                resultado.idSede());

        verify(
                reservaRepository,
                never())
                .findById(302);
    }

    @Test
    void encargadoNoDebeConsultarReservaDeOtraSede() {

        String correo =
                "encargado@bembos.test";

        Authentication authentication =
                autenticacion(
                        correo,
                        "ROLE_ENCARGADO_LOCAL");

        when(sedeAccessService
                .esAdministrador(authentication))
                .thenReturn(false);

        when(sedeAccessService
                .esEncargadoLocal(authentication))
                .thenReturn(true);

        when(sedeAccessService
                .obtenerSedeAsignada(correo))
                .thenReturn(3);

        when(reservaRepository
                .findByIdReservaAndSede_IdSede(
                        400,
                        3))
                .thenReturn(Optional.empty());

        RecursoNoEncontradoException exception =
                assertThrows(
                        RecursoNoEncontradoException.class,
                        () ->
                                reservaBackofficeService
                                        .obtenerAccesible(
                                                400,
                                                authentication));

        assertEquals(
                "Reserva no encontrada",
                exception.getMessage());

        verify(
                reservaRepository,
                never())
                .findById(400);
    }

    @Test
    void debeRechazarIdReservaNoPositivo() {

        Authentication authentication =
                autenticacion(
                        "admin@bembos.test",
                        "ROLE_ADMINISTRADOR");

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        reservaBackofficeService
                                .obtenerAccesible(
                                        0,
                                        authentication));

        verifyNoInteractions(
                reservaRepository,
                sedeAccessService);
    }

    @Test
    void administradorDebeCambiarEstadoConBloqueoEHistorial() {

        String correo =
                "admin@bembos.test";

        Authentication authentication =
                autenticacion(
                        correo,
                        "ROLE_ADMINISTRADOR");

        Reserva reserva =
                crearReserva(
                        501,
                        8,
                        "PENDIENTE");

        Usuario usuario =
                new Usuario();

        usuario.setIdUsuario(10);

        when(sedeAccessService
                .esAdministrador(authentication))
                .thenReturn(true);

        when(reservaRepository
                .findByIdReservaForUpdate(501))
                .thenReturn(
                        Optional.of(reserva));

        when(usuarioRepository
                .findByCorreoIgnoreCase(correo))
                .thenReturn(
                        Optional.of(usuario));

        when(reservaRepository
                .save(reserva))
                .thenReturn(reserva);

        ReservaDetalleResponse resultado =
                reservaBackofficeService
                        .cambiarEstado(
                                501,
                                new CambiarEstadoReservaRequest(
                                        EstadoReserva.CONFIRMADA),
                                authentication);

        assertEquals(
                "CONFIRMADA",
                resultado.estado());

        assertEquals(
                "CONFIRMADA",
                reserva.getEstado());

        assertTrue(
                reserva.getFechaActualizacion() != null);

        verify(reservaRepository)
                .findByIdReservaForUpdate(501);

        verify(
                reservaRepository,
                never())
                .findByIdReservaAndSede_IdSedeForUpdate(
                        anyInt(),
                        anyInt());

        verify(reservaService)
                .validarTransicionEstado(
                        EstadoReserva.PENDIENTE,
                        EstadoReserva.CONFIRMADA);

        ArgumentCaptor<HistorialReserva> captor =
                ArgumentCaptor.forClass(
                        HistorialReserva.class);

        verify(historialReservaRepository)
                .save(captor.capture());

        HistorialReserva historial =
                captor.getValue();

        assertSame(
                reserva,
                historial.getReserva());

        assertEquals(
                "PENDIENTE",
                historial.getEstadoAnterior());

        assertEquals(
                "CONFIRMADA",
                historial.getEstadoNuevo());

        assertEquals(
                "Estado de reserva actualizado desde BackOffice",
                historial.getObservacion());

        assertSame(
                usuario,
                historial.getUsuarioResponsable());

        assertEquals(
                reserva.getFechaActualizacion(),
                historial.getFechaCambio());
    }

    @Test
    void encargadoDebeCambiarEstadoSoloEnSuSede() {

        String correo =
                "encargado@bembos.test";

        Authentication authentication =
                autenticacion(
                        correo,
                        "ROLE_ENCARGADO_LOCAL");

        Reserva reserva =
                crearReserva(
                        502,
                        3,
                        "CONFIRMADA");

        Usuario usuario =
                new Usuario();

        usuario.setIdUsuario(11);

        when(sedeAccessService
                .esAdministrador(authentication))
                .thenReturn(false);

        when(sedeAccessService
                .esEncargadoLocal(authentication))
                .thenReturn(true);

        when(sedeAccessService
                .obtenerSedeAsignada(correo))
                .thenReturn(3);

        when(reservaRepository
                .findByIdReservaAndSede_IdSedeForUpdate(
                        502,
                        3))
                .thenReturn(
                        Optional.of(reserva));

        when(usuarioRepository
                .findByCorreoIgnoreCase(correo))
                .thenReturn(
                        Optional.of(usuario));

        when(reservaRepository
                .save(reserva))
                .thenReturn(reserva);

        ReservaDetalleResponse resultado =
                reservaBackofficeService
                        .cambiarEstado(
                                502,
                                new CambiarEstadoReservaRequest(
                                        EstadoReserva.EN_PROCESO),
                                authentication);

        assertEquals(
                "EN_PROCESO",
                resultado.estado());

        verify(reservaRepository)
                .findByIdReservaAndSede_IdSedeForUpdate(
                        502,
                        3);

        verify(
                reservaRepository,
                never())
                .findByIdReservaForUpdate(502);

        verify(reservaService)
                .validarTransicionEstado(
                        EstadoReserva.CONFIRMADA,
                        EstadoReserva.EN_PROCESO);

        verify(historialReservaRepository)
                .save(
                        any(HistorialReserva.class));
    }

    @Test
    void encargadoNoDebeCambiarReservaDeOtraSede() {

        String correo =
                "encargado@bembos.test";

        Authentication authentication =
                autenticacion(
                        correo,
                        "ROLE_ENCARGADO_LOCAL");

        when(sedeAccessService
                .esAdministrador(authentication))
                .thenReturn(false);

        when(sedeAccessService
                .esEncargadoLocal(authentication))
                .thenReturn(true);

        when(sedeAccessService
                .obtenerSedeAsignada(correo))
                .thenReturn(3);

        when(reservaRepository
                .findByIdReservaAndSede_IdSedeForUpdate(
                        503,
                        3))
                .thenReturn(Optional.empty());

        RecursoNoEncontradoException exception =
                assertThrows(
                        RecursoNoEncontradoException.class,
                        () ->
                                reservaBackofficeService
                                        .cambiarEstado(
                                                503,
                                                new CambiarEstadoReservaRequest(
                                                        EstadoReserva.CONFIRMADA),
                                                authentication));

        assertEquals(
                "Reserva no encontrada",
                exception.getMessage());

        verify(
                reservaRepository,
                never())
                .findByIdReservaForUpdate(503);

        verifyNoInteractions(
                reservaService,
                historialReservaRepository,
                usuarioRepository);
    }

    @Test
    void debeResponderConflictAnteTransicionNoPermitida() {

        String correo =
                "admin@bembos.test";

        Authentication authentication =
                autenticacion(
                        correo,
                        "ROLE_ADMINISTRADOR");

        Reserva reserva =
                crearReserva(
                        504,
                        1,
                        "COMPLETADA");

        when(sedeAccessService
                .esAdministrador(authentication))
                .thenReturn(true);

        when(reservaRepository
                .findByIdReservaForUpdate(504))
                .thenReturn(
                        Optional.of(reserva));

        doThrow(
                new IllegalArgumentException(
                        "Transicion de estado de reserva no permitida"))
                .when(reservaService)
                .validarTransicionEstado(
                        EstadoReserva.COMPLETADA,
                        EstadoReserva.PENDIENTE);

        ConflictoReservaException exception =
                assertThrows(
                        ConflictoReservaException.class,
                        () ->
                                reservaBackofficeService
                                        .cambiarEstado(
                                                504,
                                                new CambiarEstadoReservaRequest(
                                                        EstadoReserva.PENDIENTE),
                                                authentication));

        assertEquals(
                "La transicion de estado de la reserva no esta permitida",
                exception.getMessage());

        assertEquals(
                "COMPLETADA",
                reserva.getEstado());

        verify(
                reservaRepository,
                never())
                .save(any(Reserva.class));

        verifyNoInteractions(
                historialReservaRepository,
                usuarioRepository);
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

    private Reserva crearReserva(
            Integer idReserva,
            Integer idSede,
            String estado) {

        Sede sede =
                new Sede();

        sede.setIdSede(
                idSede);

        Salon salon =
                new Salon();

        salon.setIdSalon(
                idSede * 10);

        salon.setSede(
                sede);

        Reserva reserva =
                new Reserva();

        reserva.setIdReserva(
                idReserva);

        reserva.setCodigoReserva(
                "BEM-2026-" + idReserva);

        reserva.setEstado(
                estado);

        reserva.setSede(
                sede);

        reserva.setSalon(
                salon);

        reserva.setFechaEvento(
                LocalDate.of(
                        2026,
                        12,
                        20));

        reserva.setHoraInicio(
                LocalTime.of(
                        14,
                        0));

        reserva.setHoraFin(
                LocalTime.of(
                        18,
                        0));

        reserva.setCantidadInvitados(
                40);

        reserva.setMontoBase(
                new BigDecimal("500.00"));

        reserva.setMontoServicios(
                new BigDecimal("250.00"));

        reserva.setMontoTotal(
                new BigDecimal("750.00"));

        reserva.setPorcentajeDeposito(
                new BigDecimal("0.50"));

        reserva.setMontoDeposito(
                new BigDecimal("375.00"));

        reserva.setReservaServicios(
                List.of());

        reserva.setFechaCreacion(
                LocalDateTime.of(
                        2026,
                        10,
                        16,
                        17,
                        0));

        reserva.setFechaActualizacion(
                LocalDateTime.of(
                        2026,
                        10,
                        16,
                        17,
                        0));

        return reserva;
    }
}