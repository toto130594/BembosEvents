package com.bembosevents.ai.tool;

import com.bembosevents.ai.draft.AiConfirmReservationDraftInput;
import com.bembosevents.ai.draft.AiReservationDraftConfirmation;
import com.bembosevents.ai.draft.AiReservationDraftService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiConfirmReservationDraftToolTest {

    @Test
    void delegaConfirmacionConIdentidadServerSide() {
        AiReservationDraftService service =
                mock(
                        AiReservationDraftService.class);

        AiConfirmReservationDraftTool tool =
                new AiConfirmReservationDraftTool(
                        service);

        AiToolContext context =
                new AiToolContext(
                        "cliente@bembos.test",
                        10);

        AiConfirmReservationDraftInput input =
                new AiConfirmReservationDraftInput();

        AiReservationDraftConfirmation expected =
                new AiReservationDraftConfirmation(
                        null,
                        null);

        when(service.confirmar(
                "cliente@bembos.test",
                10))
                .thenReturn(
                        expected);

        AiReservationDraftConfirmation result =
                tool.ejecutar(
                        context,
                        input);

        assertSame(
                expected,
                result);

        verify(service)
                .confirmar(
                        "cliente@bembos.test",
                        10);
    }
}