package com.bembosevents.ai.draft;

import com.bembosevents.dto.catalogo.SalonPublicoResponse;
import com.bembosevents.dto.chat.ConversacionDetalleResponse;
import com.bembosevents.dto.reserva.CrearReservaRequest;
import com.bembosevents.dto.reserva.CrearReservaServicioRequest;
import com.bembosevents.dto.reserva.ReservaCreadaResponse;
import com.bembosevents.entity.BorradorReservaConversacional;
import com.bembosevents.entity.BorradorReservaServicio;
import com.bembosevents.entity.EstadoBorradorReservaConversacional;
import com.bembosevents.repository.BorradorReservaConversacionalRepository;
import com.bembosevents.repository.BorradorReservaServicioRepository;
import com.bembosevents.service.CatalogoPublicoService;
import com.bembosevents.service.ChatClienteService;
import com.bembosevents.service.ReservaClienteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class AiReservationDraftServiceImpl
        implements AiReservationDraftService {

    private final ChatClienteService chatClienteService;
    private final CatalogoPublicoService catalogoPublicoService;
    private final ReservaClienteService reservaClienteService;
    private final BorradorReservaConversacionalRepository draftRepository;
    private final BorradorReservaServicioRepository draftServiceRepository;

    public AiReservationDraftServiceImpl(
            ChatClienteService chatClienteService,
            CatalogoPublicoService catalogoPublicoService,
            ReservaClienteService reservaClienteService,
            BorradorReservaConversacionalRepository draftRepository,
            BorradorReservaServicioRepository draftServiceRepository) {

        this.chatClienteService =
                chatClienteService;

        this.catalogoPublicoService =
                catalogoPublicoService;

        this.reservaClienteService =
                reservaClienteService;

        this.draftRepository =
                draftRepository;

        this.draftServiceRepository =
                draftServiceRepository;
    }

    @Override
    public AiReservationDraftResponse obtenerOCrear(
            String correoCliente,
            Integer idConversacion) {

        ConversacionDetalleResponse conversacion =
                exigirConversacionBot(
                        correoCliente,
                        idConversacion);

        BorradorReservaConversacional draft =
                draftRepository
                        .findByIdConversacionAndIdCliente(
                                conversacion.idConversacion(),
                                conversacion.idCliente())
                        .orElseGet(() ->
                                crearDraft(
                                        conversacion));

        return map(
                draft,
                servicios(draft));
    }

    @Override
    public AiReservationDraftResponse actualizar(
            String correoCliente,
            Integer idConversacion,
            AiReservationDraftPatch patch) {

        if (patch == null) {
            throw new IllegalArgumentException(
                    "Los cambios del borrador son obligatorios");
        }

        ConversacionDetalleResponse conversacion =
                exigirConversacionBot(
                        correoCliente,
                        idConversacion);

        BorradorReservaConversacional draft =
                draftRepository
                        .findByIdConversacionAndIdCliente(
                                conversacion.idConversacion(),
                                conversacion.idCliente())
                        .orElseGet(() ->
                                crearDraft(
                                        conversacion));

        exigirAbierto(draft);

        aplicarUbicacion(
                conversacion,
                draft,
                patch);

        aplicarCatalogo(
                draft,
                patch);

        if (patch.fechaEvento() != null) {
            draft.setFechaEvento(
                    patch.fechaEvento());
        }

        if (patch.horaInicio() != null) {
            draft.setHoraInicio(
                    patch.horaInicio());
        }

        if (patch.horaFin() != null) {
            draft.setHoraFin(
                    patch.horaFin());
        }

        if (patch.cantidadInvitados() != null) {
            draft.setCantidadInvitados(
                    patch.cantidadInvitados());
        }

        validarHorarioFinal(draft);

        draft.setFechaActualizacion(
                LocalDateTime.now());

        draft =
                draftRepository.save(draft);

        if (patch.limpiarServicios()) {
            draftServiceRepository
                    .deleteByIdBorrador(
                            draft.getIdBorrador());
        }
        else if (patch.serviciosAdicionales() != null) {
            reemplazarServicios(
                    draft,
                    patch.serviciosAdicionales());
        }

        return map(
                draft,
                servicios(draft));
    }

    @Override
    public AiReservationDraftConfirmation confirmar(
            String correoCliente,
            Integer idConversacion) {

        ConversacionDetalleResponse conversacion =
                exigirConversacionBot(
                        correoCliente,
                        idConversacion);

        BorradorReservaConversacional draft =
                draftRepository
                        .findByIdConversacionAndIdCliente(
                                conversacion.idConversacion(),
                                conversacion.idCliente())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "No existe un borrador para confirmar"));

        exigirAbierto(draft);

        List<String> faltantes =
                camposFaltantes(draft);

        if (!faltantes.isEmpty()) {
            throw new IllegalArgumentException(
                    "El borrador esta incompleto. Faltan: "
                            + String.join(
                                    ", ",
                                    faltantes));
        }

        List<BorradorReservaServicio> detalles =
                servicios(draft);

        List<CrearReservaServicioRequest> servicios =
                detalles
                        .stream()
                        .map(detalle ->
                                new CrearReservaServicioRequest(
                                        detalle.getIdServicioAdicional(),
                                        detalle.getCantidad()))
                        .toList();

        CrearReservaRequest request =
                new CrearReservaRequest(
                        draft.getIdSede(),
                        draft.getIdSalon(),
                        draft.getIdPaquete(),
                        draft.getIdTematica(),
                        draft.getFechaEvento(),
                        draft.getHoraInicio(),
                        draft.getHoraFin(),
                        draft.getCantidadInvitados(),
                        servicios);

        ReservaCreadaResponse reserva =
                reservaClienteService.crear(
                        correoCliente,
                        request);

        LocalDateTime now =
                LocalDateTime.now();

        draft.setEstado(
                EstadoBorradorReservaConversacional.CONFIRMADO);

        draft.setIdReservaConfirmada(
                reserva.idReserva());

        draft.setFechaConfirmacion(now);
        draft.setFechaActualizacion(now);

        draft =
                draftRepository.save(draft);

        return new AiReservationDraftConfirmation(
                map(
                        draft,
                        detalles),
                reserva);
    }

    private ConversacionDetalleResponse exigirConversacionBot(
            String correoCliente,
            Integer idConversacion) {

        ConversacionDetalleResponse conversacion =
                chatClienteService.obtenerPropia(
                        correoCliente,
                        idConversacion);

        if (!"BOT".equals(
                conversacion.estado())) {

            throw new IllegalStateException(
                    "El borrador conversacional solo puede ser gestionado mientras la conversacion esta en BOT");
        }

        return conversacion;
    }

    private BorradorReservaConversacional crearDraft(
            ConversacionDetalleResponse conversacion) {

        LocalDateTime now =
                LocalDateTime.now();

        BorradorReservaConversacional draft =
                new BorradorReservaConversacional();

        draft.setIdConversacion(
                conversacion.idConversacion());

        draft.setIdCliente(
                conversacion.idCliente());

        draft.setIdSede(
                conversacion.idSede());

        draft.setEstado(
                EstadoBorradorReservaConversacional.ABIERTO);

        draft.setFechaCreacion(now);
        draft.setFechaActualizacion(now);

        return draftRepository.save(draft);
    }

    private void aplicarUbicacion(
            ConversacionDetalleResponse conversacion,
            BorradorReservaConversacional draft,
            AiReservationDraftPatch patch) {

        if (patch.idSede() != null) {
            catalogoPublicoService
                    .obtenerSede(
                            patch.idSede());

            if (conversacion.idSede() != null
                    && !conversacion.idSede()
                            .equals(
                                    patch.idSede())) {

                throw new IllegalArgumentException(
                        "La sede no coincide con la sede asociada a la conversacion");
            }

            draft.setIdSede(
                    patch.idSede());
        }

        if (patch.idSalon() != null) {
            SalonPublicoResponse salon =
                    catalogoPublicoService
                            .obtenerSalon(
                                    patch.idSalon());

            Integer effectiveSede =
                    draft.getIdSede() != null
                            ? draft.getIdSede()
                            : conversacion.idSede();

            if (effectiveSede != null
                    && !effectiveSede.equals(
                            salon.idSede())) {

                throw new IllegalArgumentException(
                        "El salon no pertenece a la sede del borrador");
            }

            if (draft.getIdSede() == null) {
                draft.setIdSede(
                        salon.idSede());
            }

            draft.setIdSalon(
                    salon.idSalon());
        }
    }

    private void aplicarCatalogo(
            BorradorReservaConversacional draft,
            AiReservationDraftPatch patch) {

        if (patch.limpiarPaquete()) {
            draft.setIdPaquete(null);
        }
        else if (patch.idPaquete() != null) {
            catalogoPublicoService
                    .obtenerPaquete(
                            patch.idPaquete());

            draft.setIdPaquete(
                    patch.idPaquete());
        }

        if (patch.limpiarTematica()) {
            draft.setIdTematica(null);
        }
        else if (patch.idTematica() != null) {
            catalogoPublicoService
                    .obtenerTematica(
                            patch.idTematica());

            draft.setIdTematica(
                    patch.idTematica());
        }
    }

    private void reemplazarServicios(
            BorradorReservaConversacional draft,
            List<AiDraftServiceSelection> requested) {

        draftServiceRepository
                .deleteByIdBorrador(
                        draft.getIdBorrador());

        if (requested.isEmpty()) {
            return;
        }

        Set<Integer> seen =
                new HashSet<>();

        List<BorradorReservaServicio> entities =
                new ArrayList<>();

        for (AiDraftServiceSelection selection : requested) {
            if (selection == null) {
                throw new IllegalArgumentException(
                        "Un servicio del borrador no puede ser nulo");
            }

            if (!seen.add(
                    selection.idServicioAdicional())) {

                throw new IllegalArgumentException(
                        "No se puede repetir un servicio adicional en el borrador");
            }

            catalogoPublicoService
                    .obtenerServicioAdicional(
                            selection.idServicioAdicional());

            BorradorReservaServicio item =
                    new BorradorReservaServicio();

            item.setIdBorrador(
                    draft.getIdBorrador());

            item.setIdServicioAdicional(
                    selection.idServicioAdicional());

            item.setCantidad(
                    selection.cantidad());

            entities.add(item);
        }

        draftServiceRepository.saveAll(
                entities);
    }

    private void validarHorarioFinal(
            BorradorReservaConversacional draft) {

        if (draft.getHoraInicio() != null
                && draft.getHoraFin() != null
                && !draft.getHoraInicio()
                        .isBefore(
                                draft.getHoraFin())) {

            throw new IllegalArgumentException(
                    "La hora de inicio debe ser anterior a la hora de fin");
        }
    }

    private void exigirAbierto(
            BorradorReservaConversacional draft) {

        if (draft.getEstado()
                != EstadoBorradorReservaConversacional.ABIERTO) {

            throw new IllegalStateException(
                    "El borrador ya fue confirmado");
        }
    }

    private List<BorradorReservaServicio> servicios(
            BorradorReservaConversacional draft) {

        if (draft.getIdBorrador() == null) {
            return List.of();
        }

        return draftServiceRepository
                .findByIdBorradorOrderByIdBorradorReservaServicioAsc(
                        draft.getIdBorrador());
    }

    private List<String> camposFaltantes(
            BorradorReservaConversacional draft) {

        List<String> faltantes =
                new ArrayList<>();

        if (draft.getIdSede() == null) {
            faltantes.add("sede");
        }

        if (draft.getIdSalon() == null) {
            faltantes.add("salon");
        }

        if (draft.getFechaEvento() == null) {
            faltantes.add("fechaEvento");
        }

        if (draft.getHoraInicio() == null) {
            faltantes.add("horaInicio");
        }

        if (draft.getHoraFin() == null) {
            faltantes.add("horaFin");
        }

        if (draft.getCantidadInvitados() == null) {
            faltantes.add("cantidadInvitados");
        }

        return List.copyOf(
                faltantes);
    }

    private AiReservationDraftResponse map(
            BorradorReservaConversacional draft,
            List<BorradorReservaServicio> detalles) {

        List<String> faltantes =
                camposFaltantes(draft);

        List<AiDraftServiceSelection> servicios =
                detalles
                        .stream()
                        .map(detalle ->
                                new AiDraftServiceSelection(
                                        detalle.getIdServicioAdicional(),
                                        detalle.getCantidad()))
                        .toList();

        return new AiReservationDraftResponse(
                draft.getIdBorrador(),
                draft.getIdConversacion(),
                draft.getIdCliente(),
                draft.getIdSede(),
                draft.getIdSalon(),
                draft.getIdPaquete(),
                draft.getIdTematica(),
                draft.getFechaEvento(),
                draft.getHoraInicio(),
                draft.getHoraFin(),
                draft.getCantidadInvitados(),
                servicios,
                draft.getEstado().name(),
                faltantes.isEmpty()
                        && draft.getEstado()
                                == EstadoBorradorReservaConversacional.ABIERTO,
                faltantes,
                draft.getIdReservaConfirmada(),
                draft.getFechaCreacion(),
                draft.getFechaActualizacion(),
                draft.getFechaConfirmacion());
    }
}