package com.bembosevents.ai.tool;

import com.bembosevents.ai.draft.AiReservationDraftPatch;
import com.bembosevents.ai.draft.AiReservationDraftResponse;
import com.bembosevents.ai.draft.AiReservationDraftService;
import org.springframework.stereotype.Component;

@Component
public class AiUpdateReservationDraftTool
        implements AiTool<AiReservationDraftPatch, AiReservationDraftResponse> {

    private final AiReservationDraftService draftService;

    public AiUpdateReservationDraftTool(
            AiReservationDraftService draftService) {

        this.draftService =
                draftService;
    }

    @Override
    public AiToolType type() {
        return AiToolType.ACTUALIZAR_BORRADOR_RESERVA;
    }

    @Override
    public Class<AiReservationDraftPatch> inputType() {
        return AiReservationDraftPatch.class;
    }

    @Override
    public AiReservationDraftResponse ejecutar(
            AiToolContext context,
            AiReservationDraftPatch input) {

        if (context == null) {
            throw new IllegalArgumentException(
                    "El contexto de IA es obligatorio");
        }

        return draftService.actualizar(
                context.correoCliente(),
                context.idConversacion(),
                input);
    }
}