package com.bembosevents.ai.draft;

import com.bembosevents.dto.catalogo.SalonPublicoResponse;
import com.bembosevents.dto.catalogo.SedePublicaResponse;
import com.bembosevents.dto.chat.ConversacionDetalleResponse;
import com.bembosevents.dto.reserva.CrearReservaRequest;
import com.bembosevents.dto.reserva.ReservaCreadaResponse;
import com.bembosevents.entity.BorradorReservaConversacional;
import com.bembosevents.entity.BorradorReservaServicio;
import com.bembosevents.entity.EstadoBorradorReservaConversacional;
import com.bembosevents.repository.BorradorReservaConversacionalRepository;
import com.bembosevents.repository.BorradorReservaServicioRepository;
import com.bembosevents.service.CatalogoPublicoService;
import com.bembosevents.service.ChatClienteService;
import com.bembosevents.service.ReservaClienteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiReservationDraftServiceImplTest {

    @Mock
    private ChatClienteService chatClienteService;

    @Mock
    private CatalogoPublicoService catalogoPublicoService;

    @Mock
    private ReservaClienteService reservaClienteService;

    @Mock
    private BorradorReservaConversacionalRepository draftRepository;

    @Mock
    private BorradorReservaServicioRepository draftServiceRepository;

    @Test
    void actualizaBorradorPersistenteLigadoAConversacionCliente() {
        AiReservationDraftServiceImpl service =
                service();

        when(chatClienteService.obtenerPropia(
                "cliente@bembos.test",
                10))
                .thenReturn(
                        conversacion(
                                10,
                                7,
                                3,
                                "BOT"));

        when(draftRepository.findByIdConversacionAndIdCliente(
                10,
                7))
                .thenReturn(
                        Optional.empty());

        when(draftRepository.save(any()))
                .thenAnswer(invocation -> {
                    BorradorReservaConversacional draft =
                            invocation.getArgument(0);

                    if (draft.getIdBorrador() == null) {
                        draft.setIdBorrador(100);
                    }

                    return draft;
                });

        when(catalogoPublicoService.obtenerSede(3))
                .thenReturn(
                        new SedePublicaResponse(
                                3,
                                "Sede",
                                "Direccion",
                                "Distrito",
                                "999"));

        when(catalogoPublicoService.obtenerSalon(8))
                .thenReturn(
                        new SalonPublicoResponse(
                                8,
                                3,
                                "Salon",
                                "Salon activo",
                                40));

        when(draftServiceRepository
                .findByIdBorradorOrderByIdBorradorReservaServicioAsc(
                        100))
                .thenReturn(
                        List.of());

        AiReservationDraftResponse result =
                service.actualizar(
                        "cliente@bembos.test",
                        10,
                        new AiReservationDraftPatch(
                                3,
                                8,
                                null,
                                null,
                                LocalDate.of(2026, 11, 20),
                                LocalTime.of(16, 0),
                                LocalTime.of(18, 0),
                                25,
                                List.of(),
                                false,
                                false,
                                true));

        assertEquals(100, result.idBorrador());
        assertEquals(10, result.idConversacion());
        assertEquals(7, result.idCliente());
        assertEquals(3, result.idSede());
        assertEquals(8, result.idSalon());
        assertEquals(25, result.cantidadInvitados());
        assertTrue(result.listoParaConfirmar());
        assertTrue(result.camposFaltantes().isEmpty());

        verify(draftServiceRepository)
                .deleteByIdBorrador(100);
    }

    @Test
    void confirmacionIncompletaNoCreaReserva() {
        AiReservationDraftServiceImpl service =
                service();

        BorradorReservaConversacional draft =
                draftAbierto();

        draft.setIdSede(3);
        draft.setIdSalon(8);

        when(chatClienteService.obtenerPropia(
                "cliente@bembos.test",
                10))
                .thenReturn(
                        conversacion(
                                10,
                                7,
                                3,
                                "BOT"));

        when(draftRepository.findByIdConversacionAndIdCliente(
                10,
                7))
                .thenReturn(
                        Optional.of(draft));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.confirmar(
                        "cliente@bembos.test",
                        10));

        verify(reservaClienteService, never())
                .crear(
                        any(),
                        any());
    }

    @Test
    void confirmarDelegaAlServicioRealYMarcaDraftConfirmado() {
        AiReservationDraftServiceImpl service =
                service();

        BorradorReservaConversacional draft =
                draftAbierto();

        draft.setIdSede(3);
        draft.setIdSalon(8);
        draft.setIdPaquete(2);
        draft.setIdTematica(5);
        draft.setFechaEvento(
                LocalDate.of(2026, 11, 20));
        draft.setHoraInicio(
                LocalTime.of(16, 0));
        draft.setHoraFin(
                LocalTime.of(18, 0));
        draft.setCantidadInvitados(25);

        BorradorReservaServicio item =
                new BorradorReservaServicio();

        item.setIdBorrador(100);
        item.setIdServicioAdicional(9);
        item.setCantidad(2);

        when(chatClienteService.obtenerPropia(
                "cliente@bembos.test",
                10))
                .thenReturn(
                        conversacion(
                                10,
                                7,
                                3,
                                "BOT"));

        when(draftRepository.findByIdConversacionAndIdCliente(
                10,
                7))
                .thenReturn(
                        Optional.of(draft));

        when(draftServiceRepository
                .findByIdBorradorOrderByIdBorradorReservaServicioAsc(
                        100))
                .thenReturn(
                        List.of(item));

        when(reservaClienteService.crear(
                any(),
                any()))
                .thenReturn(
                        reservaCreada());

        when(draftRepository.save(any()))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        AiReservationDraftConfirmation result =
                service.confirmar(
                        "cliente@bembos.test",
                        10);

        assertEquals(
                500,
                result.reserva()
                        .idReserva());

        assertEquals(
                "CONFIRMADO",
                result.borrador()
                        .estado());

        assertEquals(
                500,
                result.borrador()
                        .idReservaConfirmada());

        assertFalse(
                result.borrador()
                        .listoParaConfirmar());

        ArgumentCaptor<CrearReservaRequest> captor =
                ArgumentCaptor.forClass(
                        CrearReservaRequest.class);

        verify(reservaClienteService)
                .crear(
                        org.mockito.ArgumentMatchers.eq(
                                "cliente@bembos.test"),
                        captor.capture());

        CrearReservaRequest request =
                captor.getValue();

        assertEquals(3, request.idSede());
        assertEquals(8, request.idSalon());
        assertEquals(2, request.idPaquete());
        assertEquals(5, request.idTematica());
        assertEquals(25, request.cantidadInvitados());
        assertEquals(1, request.serviciosAdicionales().size());
        assertEquals(
                9,
                request.serviciosAdicionales()
                        .get(0)
                        .idServicioAdicional());
        assertEquals(
                2,
                request.serviciosAdicionales()
                        .get(0)
                        .cantidad());
    }

    @Test
    void noGestionaDraftSiConversacionYaNoEstaEnBot() {
        AiReservationDraftServiceImpl service =
                service();

        when(chatClienteService.obtenerPropia(
                "cliente@bembos.test",
                10))
                .thenReturn(
                        conversacion(
                                10,
                                7,
                                3,
                                "EN_ATENCION"));

        assertThrows(
                IllegalStateException.class,
                () -> service.obtenerOCrear(
                        "cliente@bembos.test",
                        10));

        verify(draftRepository, never())
                .findByIdConversacionAndIdCliente(
                        any(),
                        any());
    }

    private AiReservationDraftServiceImpl service() {
        return new AiReservationDraftServiceImpl(
                chatClienteService,
                catalogoPublicoService,
                reservaClienteService,
                draftRepository,
                draftServiceRepository);
    }

    private ConversacionDetalleResponse conversacion(
            Integer idConversacion,
            Integer idCliente,
            Integer idSede,
            String estado) {

        return new ConversacionDetalleResponse(
                idConversacion,
                idCliente,
                idSede,
                estado,
                null,
                null,
                null,
                List.of());
    }

    private BorradorReservaConversacional draftAbierto() {
        BorradorReservaConversacional draft =
                new BorradorReservaConversacional();

        draft.setIdBorrador(100);
        draft.setIdConversacion(10);
        draft.setIdCliente(7);
        draft.setEstado(
                EstadoBorradorReservaConversacional.ABIERTO);
        draft.setFechaCreacion(
                LocalDateTime.of(
                        2026,
                        10,
                        22,
                        12,
                        0));
        draft.setFechaActualizacion(
                LocalDateTime.of(
                        2026,
                        10,
                        22,
                        12,
                        0));

        return draft;
    }

    private ReservaCreadaResponse reservaCreada() {
        return new ReservaCreadaResponse(
                500,
                "BEM-2026-000500",
                "PENDIENTE",
                3,
                8,
                2,
                5,
                LocalDate.of(2026, 11, 20),
                LocalTime.of(16, 0),
                LocalTime.of(18, 0),
                25,
                new BigDecimal("500.00"),
                new BigDecimal("100.00"),
                new BigDecimal("600.00"),
                new BigDecimal("50.00"),
                new BigDecimal("300.00"),
                LocalDateTime.of(
                        2026,
                        10,
                        22,
                        12,
                        30));
    }
}