package com.bembosevents.ai.tool;

import com.bembosevents.dto.catalogo.DisponibilidadSalonResponse;
import com.bembosevents.dto.catalogo.SalonPublicoResponse;
import com.bembosevents.dto.chat.ConversacionDetalleResponse;
import com.bembosevents.service.CatalogoPublicoService;
import com.bembosevents.service.ChatClienteService;
import com.bembosevents.service.DisponibilidadSalonService;
import org.springframework.stereotype.Component;

@Component
public class AiAvailabilityTool
        implements AiTool<AiAvailabilityInput, AiAvailabilityResult> {

    private final ChatClienteService chatClienteService;
    private final CatalogoPublicoService catalogoPublicoService;
    private final DisponibilidadSalonService disponibilidadSalonService;

    public AiAvailabilityTool(
            ChatClienteService chatClienteService,
            CatalogoPublicoService catalogoPublicoService,
            DisponibilidadSalonService disponibilidadSalonService) {

        this.chatClienteService =
                chatClienteService;

        this.catalogoPublicoService =
                catalogoPublicoService;

        this.disponibilidadSalonService =
                disponibilidadSalonService;
    }

    @Override
    public AiToolType type() {
        return AiToolType.CONSULTAR_DISPONIBILIDAD;
    }

    @Override
    public Class<AiAvailabilityInput> inputType() {
        return AiAvailabilityInput.class;
    }

    @Override
    public AiAvailabilityResult ejecutar(
            AiToolContext context,
            AiAvailabilityInput input) {

        if (context == null) {
            throw new IllegalArgumentException(
                    "El contexto de IA es obligatorio");
        }

        if (input == null) {
            throw new IllegalArgumentException(
                    "La entrada de disponibilidad es obligatoria");
        }

        ConversacionDetalleResponse conversacion =
                chatClienteService.obtenerPropia(
                        context.correoCliente(),
                        context.idConversacion());

        if (!"BOT".equals(conversacion.estado())) {
            throw new IllegalStateException(
                    "La IA solo puede ejecutar tools mientras la conversacion esta en BOT");
        }

        SalonPublicoResponse salon =
                catalogoPublicoService.obtenerSalon(
                        input.idSalon());

        if (conversacion.idSede() != null
                && !conversacion.idSede().equals(
                        salon.idSede())) {

            throw new IllegalArgumentException(
                    "El salon no pertenece a la sede asociada a la conversacion");
        }

        DisponibilidadSalonResponse disponibilidad =
                disponibilidadSalonService.consultar(
                        input.idSalon(),
                        input.fecha(),
                        input.horaInicio(),
                        input.horaFin());

        return new AiAvailabilityResult(
                conversacion.idConversacion(),
                salon.idSede(),
                salon.idSalon(),
                salon.nombre(),
                salon.capacidad(),
                disponibilidad.fecha(),
                disponibilidad.horaInicio(),
                disponibilidad.horaFin(),
                disponibilidad.disponible(),
                disponibilidad.motivo());
    }
}