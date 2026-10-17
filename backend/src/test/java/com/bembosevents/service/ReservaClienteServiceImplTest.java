package com.bembosevents.service;

import com.bembosevents.config.ReservaProperties;
import com.bembosevents.domain.EstadoReserva;
import com.bembosevents.dto.catalogo.DisponibilidadSalonResponse;
import com.bembosevents.dto.reserva.CrearReservaRequest;
import com.bembosevents.dto.reserva.CrearReservaServicioRequest;
import com.bembosevents.dto.reserva.ReservaCreadaResponse;
import com.bembosevents.dto.reserva.ReservaDetalleResponse;
import com.bembosevents.dto.reserva.ReservaResumenResponse;
import com.bembosevents.entity.Cliente;
import com.bembosevents.entity.HistorialReserva;
import com.bembosevents.entity.Reserva;
import com.bembosevents.entity.ReservaServicio;
import com.bembosevents.entity.Salon;
import com.bembosevents.entity.Sede;
import com.bembosevents.entity.ServicioAdicional;
import com.bembosevents.entity.Usuario;
import com.bembosevents.exception.ConflictoReservaException;
import com.bembosevents.exception.RecursoNoEncontradoException;
import com.bembosevents.repository.ClienteRepository;
import com.bembosevents.repository.HistorialReservaRepository;
import com.bembosevents.repository.PaqueteRepository;
import com.bembosevents.repository.ReservaRepository;
import com.bembosevents.repository.ReservaServicioRepository;
import com.bembosevents.repository.SalonRepository;
import com.bembosevents.repository.SedeRepository;
import com.bembosevents.repository.ServicioAdicionalRepository;
import com.bembosevents.repository.TematicaRepository;
import com.bembosevents.service.impl.ReservaClienteServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservaClienteServiceImplTest {

    private static final String CORREO =
            "cliente@bembosevents.com";

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private SedeRepository sedeRepository;

    @Mock
    private SalonRepository salonRepository;

    @Mock
    private PaqueteRepository paqueteRepository;

    @Mock
    private TematicaRepository tematicaRepository;

    @Mock
    private ServicioAdicionalRepository servicioAdicionalRepository;

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private ReservaServicioRepository reservaServicioRepository;

    @Mock
    private HistorialReservaRepository historialReservaRepository;

    @Mock
    private DisponibilidadSalonService disponibilidadSalonService;

    @Mock
    private ReservaService reservaService;

    @Mock
    private ReservaProperties reservaProperties;

    @InjectMocks
    private ReservaClienteServiceImpl reservaClienteService;

    @Test
    void debeCalcularPrecioUsandoPrecioActualDelServicio() {

        CrearReservaServicioRequest seleccion =
                mock(CrearReservaServicioRequest.class);

        when(seleccion.idServicioAdicional())
                .thenReturn(7);

        when(seleccion.cantidad())
                .thenReturn(2);

        ServicioAdicional servicio =
                mock(ServicioAdicional.class);

        when(servicio.getPrecio())
                .thenReturn(
                        new BigDecimal("125.00"));

        prepararEscenario(
                seleccion,
                servicio,
                true);

        ReservaCreadaResponse response =
                reservaClienteService.crear(
                        CORREO,
                        crearRequest(List.of(seleccion)));

        ArgumentCaptor<Reserva> captor =
                ArgumentCaptor.forClass(
                        Reserva.class);

        verify(reservaRepository)
                .save(captor.capture());

        Reserva reserva =
                captor.getValue();

        assertEquals(
                0,
                new BigDecimal("250.00")
                        .compareTo(
                                reserva.getMontoServicios()));

        assertEquals(
                0,
                new BigDecimal("250.00")
                        .compareTo(
                                reserva.getMontoTotal()));

        assertEquals(
                0,
                new BigDecimal("250.00")
                        .compareTo(
                                response.montoServicios()));

        assertEquals(
                0,
                new BigDecimal("250.00")
                        .compareTo(
                                response.montoTotal()));
    }

    @Test
    @SuppressWarnings("unchecked")
    void debePersistirReservaServiciosEHistorial() {

        CrearReservaServicioRequest seleccion =
                mock(CrearReservaServicioRequest.class);

        when(seleccion.idServicioAdicional())
                .thenReturn(7);

        when(seleccion.cantidad())
                .thenReturn(2);

        ServicioAdicional servicio =
                mock(ServicioAdicional.class);

        when(servicio.getPrecio())
                .thenReturn(
                        new BigDecimal("125.00"));

        Escenario escenario =
                prepararEscenario(
                        seleccion,
                        servicio,
                        true);

        reservaClienteService.crear(
                CORREO,
                crearRequest(List.of(seleccion)));

        ArgumentCaptor<Reserva> reservaCaptor =
                ArgumentCaptor.forClass(
                        Reserva.class);

        verify(reservaRepository)
                .save(reservaCaptor.capture());

        Reserva reserva =
                reservaCaptor.getValue();

        ArgumentCaptor<Iterable<ReservaServicio>>
                serviciosCaptor =
                ArgumentCaptor.forClass(
                        Iterable.class);

        verify(reservaServicioRepository)
                .saveAll(
                        serviciosCaptor.capture());

        List<ReservaServicio> detalles =
                new ArrayList<>();

        serviciosCaptor
                .getValue()
                .forEach(detalles::add);

        assertEquals(
                1,
                detalles.size());

        ReservaServicio detalle =
                detalles.get(0);

        assertSame(
                reserva,
                detalle.getReserva());

        assertSame(
                servicio,
                detalle.getServicioAdicional());

        assertEquals(
                2,
                detalle.getCantidad());

        assertEquals(
                0,
                new BigDecimal("125.00")
                        .compareTo(
                                detalle.getPrecioUnitario()));

        assertEquals(
                0,
                new BigDecimal("250.00")
                        .compareTo(
                                detalle.getSubtotal()));

        ArgumentCaptor<HistorialReserva>
                historialCaptor =
                ArgumentCaptor.forClass(
                        HistorialReserva.class);

        verify(historialReservaRepository)
                .save(
                        historialCaptor.capture());

        HistorialReserva historial =
                historialCaptor.getValue();

        assertSame(
                reserva,
                historial.getReserva());

        assertNull(
                historial.getEstadoAnterior());

        assertEquals(
                "PENDIENTE",
                historial.getEstadoNuevo());

        assertEquals(
                "Reserva creada por el cliente",
                historial.getObservacion());

        assertSame(
                escenario.usuario(),
                historial.getUsuarioResponsable());
    }

    @Test
    void debeRechazarReservaCuandoHorarioNoEstaDisponible() {

        prepararEscenario(
                null,
                null,
                false);

        ConflictoReservaException exception =
                assertThrows(
                        ConflictoReservaException.class,
                        () ->
                                reservaClienteService.crear(
                                        CORREO,
                                        crearRequest(
                                                List.of())));

        assertEquals(
                "Horario ocupado",
                exception.getMessage());

        verify(
                reservaRepository,
                never())
                .siguienteValorCodigoReserva();

        verify(
                reservaRepository,
                never())
                .save(any(Reserva.class));

        verifyNoInteractions(
                reservaServicioRepository,
                historialReservaRepository);
    }

    @Test
    void debeRechazarSalonQueNoPerteneceALaSede() {

        Usuario usuario =
                mock(Usuario.class);

        Cliente cliente =
                mock(Cliente.class);

        when(cliente.getUsuario())
                .thenReturn(usuario);

        when(clienteRepository
                .findByUsuario_CorreoIgnoreCaseAndActivoTrue(
                        CORREO))
                .thenReturn(
                        Optional.of(cliente));

        Sede sedeSolicitada =
                mock(Sede.class);

        when(sedeSolicitada.getIdSede())
                .thenReturn(1);

        when(sedeRepository
                .findByIdSedeAndActivoTrue(1))
                .thenReturn(
                        Optional.of(sedeSolicitada));

        Sede otraSede =
                mock(Sede.class);

        when(otraSede.getIdSede())
                .thenReturn(2);

        Salon salon =
                mock(Salon.class);

        when(salon.getSede())
                .thenReturn(otraSede);

        when(salonRepository
                .findByIdSalonAndActivoTrueForUpdate(10))
                .thenReturn(
                        Optional.of(salon));

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                reservaClienteService.crear(
                                        CORREO,
                                        crearRequest(
                                                List.of())));

        assertEquals(
                "El salon no pertenece a la sede indicada",
                exception.getMessage());

        verifyNoInteractions(
                reservaService,
                disponibilidadSalonService);

        verify(
                reservaRepository,
                never())
                .save(any(Reserva.class));
    }

    @Test
    void debeGenerarCodigosDistintosConConsecutivosDistintos() {

        prepararEscenario(
                null,
                null,
                true);

        when(reservaRepository
                .siguienteValorCodigoReserva())
                .thenReturn(
                        42L,
                        43L);

        ReservaCreadaResponse primera =
                reservaClienteService.crear(
                        CORREO,
                        crearRequest(List.of()));

        ReservaCreadaResponse segunda =
                reservaClienteService.crear(
                        CORREO,
                        crearRequest(List.of()));

        assertNotEquals(
                primera.codigoReserva(),
                segunda.codigoReserva());

        assertTrue(
                primera.codigoReserva()
                        .matches(
                                "BEM-\\d{4}-000042"));

        assertTrue(
                segunda.codigoReserva()
                        .matches(
                                "BEM-\\d{4}-000043"));

        verify(
                reservaRepository,
                times(2))
                .siguienteValorCodigoReserva();
    }

    private Escenario prepararEscenario(
            CrearReservaServicioRequest seleccion,
            ServicioAdicional servicio,
            boolean disponible) {

        Usuario usuario =
                mock(Usuario.class);

        Cliente cliente =
                mock(Cliente.class);

        when(cliente.getUsuario())
                .thenReturn(usuario);

        when(clienteRepository
                .findByUsuario_CorreoIgnoreCaseAndActivoTrue(
                        CORREO))
                .thenReturn(
                        Optional.of(cliente));

        Sede sede =
                mock(Sede.class);

        when(sede.getIdSede())
                .thenReturn(1);

        when(sede.getActivo())
                .thenReturn(true);

        when(sedeRepository
                .findByIdSedeAndActivoTrue(1))
                .thenReturn(
                        Optional.of(sede));

        Salon salon =
                mock(Salon.class);

        when(salon.getIdSalon())
                .thenReturn(10);

        when(salon.getSede())
                .thenReturn(sede);

        when(salonRepository
                .findByIdSalonAndActivoTrueForUpdate(10))
                .thenReturn(
                        Optional.of(salon));

        DisponibilidadSalonResponse disponibilidad =
                mock(
                        DisponibilidadSalonResponse.class);

        when(disponibilidad.disponible())
                .thenReturn(disponible);

        if (!disponible) {
            when(disponibilidad.motivo())
                    .thenReturn(
                            "Horario ocupado");
        }

        when(disponibilidadSalonService
                .consultar(
                        eq(10),
                        any(LocalDate.class),
                        any(LocalTime.class),
                        any(LocalTime.class)))
                .thenReturn(
                        disponibilidad);

        if (seleccion != null) {

            when(servicioAdicionalRepository
                    .findByIdServicioAdicionalAndActivoTrue(
                            seleccion.idServicioAdicional()))
                    .thenReturn(
                            Optional.of(servicio));
        }

        if (disponible) {

            when(reservaProperties
                    .getPorcentajeDeposito())
                    .thenReturn(
                            new BigDecimal("0.50"));

            when(reservaService
                    .calcularMontoTotal(
                            any(BigDecimal.class),
                            any(BigDecimal.class)))
                    .thenAnswer(invocation -> {

                        BigDecimal base =
                                invocation.getArgument(0);

                        BigDecimal adicionales =
                                invocation.getArgument(1);

                        return base.add(
                                adicionales);
                    });

            when(reservaService
                    .calcularMontoDeposito(
                            any(BigDecimal.class)))
                    .thenAnswer(invocation -> {

                        BigDecimal total =
                                invocation.getArgument(0);

                        return total.multiply(
                                new BigDecimal("0.50"));
                    });

            when(reservaRepository
                    .siguienteValorCodigoReserva())
                    .thenReturn(42L);

            when(reservaRepository
                    .save(any(Reserva.class)))
                    .thenAnswer(invocation -> {

                        Reserva reserva =
                                invocation.getArgument(0);

                        reserva.setIdReserva(100);

                        return reserva;
                    });
        }

        return new Escenario(
                cliente,
                usuario);
    }

    private CrearReservaRequest crearRequest(
            List<CrearReservaServicioRequest> servicios) {

        return new CrearReservaRequest(
                1,
                10,
                null,
                null,
                LocalDate.of(
                        2026,
                        11,
                        20),
                LocalTime.of(
                        14,
                        0),
                LocalTime.of(
                        18,
                        0),
                40,
                servicios);
    }

    private record Escenario(
            Cliente cliente,
            Usuario usuario) {
    }

    @Test
    void debeListarSoloReservasDelClienteAutenticado() {

        Usuario usuario =
                mock(Usuario.class);

        Cliente cliente =
                mock(Cliente.class);

        when(cliente.getUsuario())
                .thenReturn(usuario);

        when(cliente.getIdCliente())
                .thenReturn(77);

        when(clienteRepository
                .findByUsuario_CorreoIgnoreCaseAndActivoTrue(
                        CORREO))
                .thenReturn(
                        Optional.of(cliente));

        Reserva reciente =
                crearReservaParaListado(
                        102,
                        "BEM-2026-000102",
                        LocalDate.of(
                                2026,
                                12,
                                22),
                        LocalDateTime.of(
                                2026,
                                10,
                                16,
                                18,
                                0));

        Reserva anterior =
                crearReservaParaListado(
                        101,
                        "BEM-2026-000101",
                        LocalDate.of(
                                2026,
                                12,
                                20),
                        LocalDateTime.of(
                                2026,
                                10,
                                16,
                                17,
                                0));

        when(reservaRepository
                .findByCliente_IdClienteOrderByFechaCreacionDesc(
                        77))
                .thenReturn(
                        List.of(
                                reciente,
                                anterior));

        List<ReservaResumenResponse> resultado =
                reservaClienteService
                        .misReservas(CORREO);

        assertEquals(
                2,
                resultado.size());

        assertEquals(
                102,
                resultado.get(0).idReserva());

        assertEquals(
                "BEM-2026-000102",
                resultado.get(0).codigoReserva());

        assertEquals(
                101,
                resultado.get(1).idReserva());

        assertEquals(
                "PENDIENTE",
                resultado.get(0).estado());

        assertEquals(
                1,
                resultado.get(0).idSede());

        assertEquals(
                10,
                resultado.get(0).idSalon());

        assertEquals(
                0,
                new BigDecimal("500.00")
                        .compareTo(
                                resultado.get(0)
                                        .montoTotal()));

        verify(reservaRepository)
                .findByCliente_IdClienteOrderByFechaCreacionDesc(
                        77);

        verify(
                reservaRepository,
                never())
                .findAll();
    }

    @Test
    void debeDevolverListaVaciaCuandoClienteNoTieneReservas() {

        Usuario usuario =
                mock(Usuario.class);

        Cliente cliente =
                mock(Cliente.class);

        when(cliente.getUsuario())
                .thenReturn(usuario);

        when(cliente.getIdCliente())
                .thenReturn(77);

        when(clienteRepository
                .findByUsuario_CorreoIgnoreCaseAndActivoTrue(
                        CORREO))
                .thenReturn(
                        Optional.of(cliente));

        when(reservaRepository
                .findByCliente_IdClienteOrderByFechaCreacionDesc(
                        77))
                .thenReturn(
                        List.of());

        List<ReservaResumenResponse> resultado =
                reservaClienteService
                        .misReservas(CORREO);

        assertTrue(
                resultado.isEmpty());

        verify(reservaRepository)
                .findByCliente_IdClienteOrderByFechaCreacionDesc(
                        77);
    }

    @Test
    void debeRechazarConsultaSinPerfilClienteActivo() {

        when(clienteRepository
                .findByUsuario_CorreoIgnoreCaseAndActivoTrue(
                        CORREO))
                .thenReturn(
                        Optional.empty());

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () ->
                                reservaClienteService
                                        .misReservas(
                                                CORREO));

        assertEquals(
                "El usuario autenticado no tiene un perfil Cliente activo",
                exception.getMessage());

        verify(
                reservaRepository,
                never())
                .findByCliente_IdClienteOrderByFechaCreacionDesc(
                        any());
    }

    private Reserva crearReservaParaListado(
            Integer idReserva,
            String codigoReserva,
            LocalDate fechaEvento,
            LocalDateTime fechaCreacion) {

        Sede sede =
                new Sede();

        sede.setIdSede(1);

        Salon salon =
                new Salon();

        salon.setIdSalon(10);
        salon.setSede(sede);

        Reserva reserva =
                new Reserva();

        reserva.setIdReserva(
                idReserva);

        reserva.setCodigoReserva(
                codigoReserva);

        reserva.setEstado(
                "PENDIENTE");

        reserva.setSede(
                sede);

        reserva.setSalon(
                salon);

        reserva.setFechaEvento(
                fechaEvento);

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

        reserva.setMontoTotal(
                new BigDecimal("500.00"));

        reserva.setMontoDeposito(
                new BigDecimal("250.00"));

        reserva.setFechaCreacion(
                fechaCreacion);

        return reserva;
    }

    @Test
    void debeConsultarDetalleDeReservaPropia() {

        Usuario usuario =
                mock(Usuario.class);

        Cliente cliente =
                mock(Cliente.class);

        when(cliente.getUsuario())
                .thenReturn(usuario);

        when(cliente.getIdCliente())
                .thenReturn(77);

        when(clienteRepository
                .findByUsuario_CorreoIgnoreCaseAndActivoTrue(
                        CORREO))
                .thenReturn(
                        Optional.of(cliente));

        Sede sede =
                new Sede();

        sede.setIdSede(1);

        Salon salon =
                new Salon();

        salon.setIdSalon(10);
        salon.setSede(sede);

        ServicioAdicional servicio =
                new ServicioAdicional();

        servicio.setIdServicioAdicional(7);

        ReservaServicio detalle =
                new ReservaServicio();

        detalle.setServicioAdicional(
                servicio);

        detalle.setCantidad(
                2);

        detalle.setPrecioUnitario(
                new BigDecimal("125.00"));

        detalle.setSubtotal(
                new BigDecimal("250.00"));

        Reserva reserva =
                new Reserva();

        reserva.setIdReserva(
                150);

        reserva.setCodigoReserva(
                "BEM-2026-000150");

        reserva.setEstado(
                "PENDIENTE");

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
                List.of(detalle));

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

        when(reservaRepository
                .findByIdReservaAndCliente_IdCliente(
                        150,
                        77))
                .thenReturn(
                        Optional.of(reserva));

        ReservaDetalleResponse resultado =
                reservaClienteService
                        .consultarPropia(
                                CORREO,
                                150);

        assertEquals(
                150,
                resultado.idReserva());

        assertEquals(
                "BEM-2026-000150",
                resultado.codigoReserva());

        assertEquals(
                "PENDIENTE",
                resultado.estado());

        assertEquals(
                1,
                resultado.idSede());

        assertEquals(
                10,
                resultado.idSalon());

        assertEquals(
                0,
                new BigDecimal("750.00")
                        .compareTo(
                                resultado.montoTotal()));

        assertEquals(
                1,
                resultado.serviciosAdicionales()
                        .size());

        assertEquals(
                7,
                resultado.serviciosAdicionales()
                        .get(0)
                        .idServicioAdicional());

        assertEquals(
                0,
                new BigDecimal("125.00")
                        .compareTo(
                                resultado
                                        .serviciosAdicionales()
                                        .get(0)
                                        .precioUnitario()));

        verify(reservaRepository)
                .findByIdReservaAndCliente_IdCliente(
                        150,
                        77);

        verify(
                reservaRepository,
                never())
                .findById(150);
    }

    @Test
    void debeOcultarReservaAjenaComoNoEncontrada() {

        Usuario usuario =
                mock(Usuario.class);

        Cliente cliente =
                mock(Cliente.class);

        when(cliente.getUsuario())
                .thenReturn(usuario);

        when(cliente.getIdCliente())
                .thenReturn(77);

        when(clienteRepository
                .findByUsuario_CorreoIgnoreCaseAndActivoTrue(
                        CORREO))
                .thenReturn(
                        Optional.of(cliente));

        when(reservaRepository
                .findByIdReservaAndCliente_IdCliente(
                        200,
                        77))
                .thenReturn(
                        Optional.empty());

        RecursoNoEncontradoException exception =
                assertThrows(
                        RecursoNoEncontradoException.class,
                        () ->
                                reservaClienteService
                                        .consultarPropia(
                                                CORREO,
                                                200));

        assertEquals(
                "Reserva no encontrada",
                exception.getMessage());

        verify(reservaRepository)
                .findByIdReservaAndCliente_IdCliente(
                        200,
                        77);

        verify(
                reservaRepository,
                never())
                .findById(200);
    }

    @Test
    void debeResponderNoEncontradaCuandoReservaNoExiste() {

        Usuario usuario =
                mock(Usuario.class);

        Cliente cliente =
                mock(Cliente.class);

        when(cliente.getUsuario())
                .thenReturn(usuario);

        when(cliente.getIdCliente())
                .thenReturn(77);

        when(clienteRepository
                .findByUsuario_CorreoIgnoreCaseAndActivoTrue(
                        CORREO))
                .thenReturn(
                        Optional.of(cliente));

        when(reservaRepository
                .findByIdReservaAndCliente_IdCliente(
                        99999,
                        77))
                .thenReturn(
                        Optional.empty());

        RecursoNoEncontradoException exception =
                assertThrows(
                        RecursoNoEncontradoException.class,
                        () ->
                                reservaClienteService
                                        .consultarPropia(
                                                CORREO,
                                                99999));

        assertEquals(
                "Reserva no encontrada",
                exception.getMessage());

        verify(reservaRepository)
                .findByIdReservaAndCliente_IdCliente(
                        99999,
                        77);
    }

    @Test
    void debeRechazarIdReservaNoPositivo() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                reservaClienteService
                                        .consultarPropia(
                                                CORREO,
                                                0));

        assertEquals(
                "El id de reserva debe ser positivo",
                exception.getMessage());

        verifyNoInteractions(
                clienteRepository,
                reservaRepository);
    }

    @Test
    void debeCancelarReservaPendiente() {

        EscenarioCancelacion escenario =
                prepararEscenarioCancelacion(
                        150,
                        "PENDIENTE");

        when(reservaRepository
                .save(escenario.reserva()))
                .thenReturn(
                        escenario.reserva());

        ReservaDetalleResponse resultado =
                reservaClienteService.cancelarPropia(
                        CORREO,
                        150);

        assertEquals(
                "CANCELADA",
                resultado.estado());

        assertEquals(
                "CANCELADA",
                escenario.reserva().getEstado());

        assertTrue(
                escenario.reserva()
                        .getFechaActualizacion() != null);

        verify(reservaService)
                .validarTransicionEstado(
                        EstadoReserva.PENDIENTE,
                        EstadoReserva.CANCELADA);

        verify(reservaRepository)
                .save(
                        escenario.reserva());

        ArgumentCaptor<HistorialReserva> historialCaptor =
                ArgumentCaptor.forClass(
                        HistorialReserva.class);

        verify(historialReservaRepository)
                .save(
                        historialCaptor.capture());

        HistorialReserva historial =
                historialCaptor.getValue();

        assertSame(
                escenario.reserva(),
                historial.getReserva());

        assertEquals(
                "PENDIENTE",
                historial.getEstadoAnterior());

        assertEquals(
                "CANCELADA",
                historial.getEstadoNuevo());

        assertEquals(
                "Reserva cancelada por el cliente",
                historial.getObservacion());

        assertSame(
                escenario.usuario(),
                historial.getUsuarioResponsable());

        assertEquals(
                escenario.reserva()
                        .getFechaActualizacion(),
                historial.getFechaCambio());
    }

    @Test
    void debeCancelarReservaConfirmada() {

        EscenarioCancelacion escenario =
                prepararEscenarioCancelacion(
                        151,
                        "CONFIRMADA");

        when(reservaRepository
                .save(escenario.reserva()))
                .thenReturn(
                        escenario.reserva());

        ReservaDetalleResponse resultado =
                reservaClienteService.cancelarPropia(
                        CORREO,
                        151);

        assertEquals(
                "CANCELADA",
                resultado.estado());

        verify(reservaService)
                .validarTransicionEstado(
                        EstadoReserva.CONFIRMADA,
                        EstadoReserva.CANCELADA);

        verify(reservaRepository)
                .save(
                        escenario.reserva());

        verify(historialReservaRepository)
                .save(
                        any(HistorialReserva.class));
    }

    @Test
    void debeRechazarCancelacionDesdeEstadoNoPermitido() {

        EscenarioCancelacion escenario =
                prepararEscenarioCancelacion(
                        152,
                        "EN_PROCESO");

        org.mockito.Mockito.doThrow(
                        new IllegalArgumentException(
                                "Transicion de estado de reserva no permitida"))
                .when(reservaService)
                .validarTransicionEstado(
                        EstadoReserva.EN_PROCESO,
                        EstadoReserva.CANCELADA);

        ConflictoReservaException exception =
                assertThrows(
                        ConflictoReservaException.class,
                        () ->
                                reservaClienteService
                                        .cancelarPropia(
                                                CORREO,
                                                152));

        assertEquals(
                "La reserva no puede cancelarse en su estado actual",
                exception.getMessage());

        assertEquals(
                "EN_PROCESO",
                escenario.reserva().getEstado());

        verify(
                reservaRepository,
                never())
                .save(
                        any(Reserva.class));

        verifyNoInteractions(
                historialReservaRepository);
    }

    @Test
    void debeOcultarReservaAjenaAlCancelar() {

        Usuario usuario =
                mock(Usuario.class);

        Cliente cliente =
                mock(Cliente.class);

        when(cliente.getUsuario())
                .thenReturn(usuario);

        when(cliente.getIdCliente())
                .thenReturn(77);

        when(clienteRepository
                .findByUsuario_CorreoIgnoreCaseAndActivoTrue(
                        CORREO))
                .thenReturn(
                        Optional.of(cliente));

        when(reservaRepository
                .findByIdReservaAndCliente_IdCliente(
                        200,
                        77))
                .thenReturn(
                        Optional.empty());

        RecursoNoEncontradoException exception =
                assertThrows(
                        RecursoNoEncontradoException.class,
                        () ->
                                reservaClienteService
                                        .cancelarPropia(
                                                CORREO,
                                                200));

        assertEquals(
                "Reserva no encontrada",
                exception.getMessage());

        verify(
                reservaRepository,
                never())
                .findById(200);

        verifyNoInteractions(
                reservaService,
                historialReservaRepository);
    }

    @Test
    void debeResponderNoEncontradaAlCancelarReservaInexistente() {

        Usuario usuario =
                mock(Usuario.class);

        Cliente cliente =
                mock(Cliente.class);

        when(cliente.getUsuario())
                .thenReturn(usuario);

        when(cliente.getIdCliente())
                .thenReturn(77);

        when(clienteRepository
                .findByUsuario_CorreoIgnoreCaseAndActivoTrue(
                        CORREO))
                .thenReturn(
                        Optional.of(cliente));

        when(reservaRepository
                .findByIdReservaAndCliente_IdCliente(
                        99999,
                        77))
                .thenReturn(
                        Optional.empty());

        RecursoNoEncontradoException exception =
                assertThrows(
                        RecursoNoEncontradoException.class,
                        () ->
                                reservaClienteService
                                        .cancelarPropia(
                                                CORREO,
                                                99999));

        assertEquals(
                "Reserva no encontrada",
                exception.getMessage());

        verifyNoInteractions(
                reservaService,
                historialReservaRepository);
    }

    private EscenarioCancelacion prepararEscenarioCancelacion(
            Integer idReserva,
            String estado) {

        Usuario usuario =
                mock(Usuario.class);

        Cliente cliente =
                mock(Cliente.class);

        when(cliente.getUsuario())
                .thenReturn(usuario);

        when(cliente.getIdCliente())
                .thenReturn(77);

        when(clienteRepository
                .findByUsuario_CorreoIgnoreCaseAndActivoTrue(
                        CORREO))
                .thenReturn(
                        Optional.of(cliente));

        Sede sede = new Sede();
        sede.setIdSede(1);

        Salon salon = new Salon();
        salon.setIdSalon(10);
        salon.setSede(sede);

        Reserva reserva = new Reserva();

        reserva.setIdReserva(
                idReserva);

        reserva.setCodigoReserva(
                "BEM-2026-" + idReserva);

        reserva.setCliente(
                cliente);

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

        reserva.setEstado(
                estado);

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

        when(reservaRepository
                .findByIdReservaAndCliente_IdCliente(
                        idReserva,
                        77))
                .thenReturn(
                        Optional.of(reserva));

        return new EscenarioCancelacion(
                cliente,
                usuario,
                reserva);
    }

    private record EscenarioCancelacion(
            Cliente cliente,
            Usuario usuario,
            Reserva reserva) {
    }
}