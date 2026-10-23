package com.bembosevents.ai.tool;

import com.bembosevents.ai.draft.AiConfirmReservationDraftInput;
import com.bembosevents.ai.draft.AiReservationDraftConfirmation;
import com.bembosevents.ai.draft.AiReservationDraftService;
import org.springframework.stereotype.Component;

@Component
public class AiConfirmReservationDraftTool
        implements AiTool<AiConfirmReservationDraftInput, AiReservationDraftConfirmation> {

    private final AiReservationDraftService draftService;

    public AiConfirmReservationDraftTool(
            AiReservationDraftService draftService) {

        this.draftService =
                draftService;
    }

    @Override
    public AiToolType type() {
        return AiToolType.CONFIRMAR_BORRADOR_RESERVA;
    }

    @Override
    public Class<AiConfirmReservationDraftInput> inputType() {
        return AiConfirmReservationDraftInput.class;
    }

    @Override
    public AiReservationDraftConfirmation ejecutar(
            AiToolContext context,
            AiConfirmReservationDraftInput input) {

        if (context == null) {
            throw new IllegalArgumentException(
                    "El contexto de IA es obligatorio");
        }

        if (input == null) {
            throw new IllegalArgumentException(
                    "La confirmacion del borrador es obligatoria");
        }

        return draftService.confirmar(
                context.correoCliente(),
                context.idConversacion());
    }
}