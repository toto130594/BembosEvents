package com.bembosevents.ai.tool;

import com.bembosevents.ai.draft.AiReservationDraftPatch;
import com.bembosevents.ai.draft.AiReservationDraftResponse;
import com.bembosevents.ai.draft.AiReservationDraftService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiUpdateReservationDraftToolTest {

    @Test
    void delegaActualizacionConIdentidadServerSide() {
        AiReservationDraftService service =
                mock(
                        AiReservationDraftService.class);

        AiUpdateReservationDraftTool tool =
                new AiUpdateReservationDraftTool(
                        service);

        AiToolContext context =
                new AiToolContext(
                        "cliente@bembos.test",
                        10);

        AiReservationDraftPatch patch =
                new AiReservationDraftPatch(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        30,
                        null,
                        false,
                        false,
                        false);

        AiReservationDraftResponse expected =
                new AiReservationDraftResponse(
                        1,
                        10,
                        7,
                        3,
                        8,
                        null,
                        null,
                        null,
                        null,
                        null,
                        30,
                        List.of(),
                        "ABIERTO",
                        false,
                        List.of(
                                "fechaEvento",
                                "horaInicio",
                                "horaFin"),
                        null,
                        null,
                        null,
                        null);

        when(service.actualizar(
                "cliente@bembos.test",
                10,
                patch))
                .thenReturn(
                        expected);

        AiReservationDraftResponse result =
                tool.ejecutar(
                        context,
                        patch);

        assertSame(
                expected,
                result);

        verify(service)
                .actualizar(
                        "cliente@bembos.test",
                        10,
                        patch);
    }
}