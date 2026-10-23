package com.bembosevents.ai.orchestration;

import com.bembosevents.ai.draft.AiReservationDraftPatch;
import com.bembosevents.ai.recommendation.AiRecommendationCriteria;
import com.bembosevents.ai.tool.AiAvailabilityInput;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeterministicAiIntentInterpreterTest {

    private final AiIntentInterpreter interpreter =
            new DeterministicAiIntentInterpreter();

    @Test
    void interpretaDisponibilidadCompleta() {
        AiIntent intent =
                interpreter.interpretar(
                        "Hay disponibilidad en salon 8 el 20/11/2026 de 16:00 a 18:00?");

        assertEquals(
                AiIntentType.CONSULTAR_DISPONIBILIDAD,
                intent.tipo());

        AiAvailabilityInput input =
                (AiAvailabilityInput)
                        intent.input();

        assertEquals(8, input.idSalon());
        assertEquals(
                LocalDate.of(
                        2026,
                        11,
                        20),
                input.fecha());
        assertEquals(
                LocalTime.of(
                        16,
                        0),
                input.horaInicio());
        assertEquals(
                LocalTime.of(
                        18,
                        0),
                input.horaFin());
    }

    @Test
    void disponibilidadIncompletaNoEjecutaTool() {
        AiIntent intent =
                interpreter.interpretar(
                        "Hay disponibilidad para un salon manana?");

        assertEquals(
                AiIntentType.CONSULTAR_CONOCIMIENTO,
                intent.tipo());
    }

    @Test
    void interpretaRecomendacionConInvitados() {
        AiIntent intent =
                interpreter.interpretar(
                        "Recomiendame un paquete para 30 invitados con superheroes");

        assertEquals(
                AiIntentType.RECOMENDAR_CATALOGO,
                intent.tipo());

        AiRecommendationCriteria criteria =
                (AiRecommendationCriteria)
                        intent.input();

        assertEquals(
                30,
                criteria.numeroInvitados());
    }

    @Test
    void interpretaActualizacionIncrementalDeBorrador() {
        AiIntent intent =
                interpreter.interpretar(
                        "Para mi reserva sede 3 salon 8, 25 invitados, "
                                + "2026-11-20 de 16:00 a 18:00, paquete 2, "
                                + "tematica 5, servicio 9 x2");

        assertEquals(
                AiIntentType.ACTUALIZAR_BORRADOR_RESERVA,
                intent.tipo());

        AiReservationDraftPatch patch =
                (AiReservationDraftPatch)
                        intent.input();

        assertEquals(3, patch.idSede());
        assertEquals(8, patch.idSalon());
        assertEquals(25, patch.cantidadInvitados());
        assertEquals(2, patch.idPaquete());
        assertEquals(5, patch.idTematica());
        assertEquals(
                1,
                patch.serviciosAdicionales()
                        .size());
        assertEquals(
                9,
                patch.serviciosAdicionales()
                        .get(0)
                        .idServicioAdicional());
        assertEquals(
                2,
                patch.serviciosAdicionales()
                        .get(0)
                        .cantidad());
    }

    @Test
    void confirmacionDebeSerExplicita() {
        AiIntent explicit =
                interpreter.interpretar(
                        "Confirmo mi reserva");

        assertEquals(
                AiIntentType.CONFIRMAR_BORRADOR_RESERVA,
                explicit.tipo());

        AiIntent ambiguous =
                interpreter.interpretar(
                        "Quizas luego confirme la reserva");

        assertFalse(
                ambiguous.tipo()
                        == AiIntentType.CONFIRMAR_BORRADOR_RESERVA);
    }

    @Test
    void preguntaDeDominioUsaKnowledge() {
        AiIntent intent =
                interpreter.interpretar(
                        "Como funciona una reserva?");

        assertEquals(
                AiIntentType.CONSULTAR_CONOCIMIENTO,
                intent.tipo());
    }

    @Test
    void saludoPermaneceChatGeneral() {
        AiIntent intent =
                interpreter.interpretar(
                        "Hola, buenas tardes");

        assertEquals(
                AiIntentType.CHAT_GENERAL,
                intent.tipo());

        assertNull(
                intent.input());
    }
}