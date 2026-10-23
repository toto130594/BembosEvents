package com.bembosevents.ai.tool;

import com.bembosevents.dto.catalogo.DisponibilidadSalonResponse;
import com.bembosevents.dto.catalogo.SalonPublicoResponse;
import com.bembosevents.dto.chat.ConversacionDetalleResponse;
import com.bembosevents.service.CatalogoPublicoService;
import com.bembosevents.service.ChatClienteService;
import com.bembosevents.service.DisponibilidadSalonService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiAvailabilityToolTest {

    @Mock
    private ChatClienteService chatClienteService;

    @Mock
    private CatalogoPublicoService catalogoPublicoService;

    @Mock
    private DisponibilidadSalonService disponibilidadSalonService;

    @Test
    void consultaDisponibilidadUsandoServiciosDeterministicos() {
        AiAvailabilityTool tool =
                tool();

        AiToolContext context =
                new AiToolContext(
                        "cliente@bembos.test",
                        21);

        AiAvailabilityInput input =
                input();

        when(chatClienteService.obtenerPropia(
                "cliente@bembos.test",
                21))
                .thenReturn(
                        conversacion(
                                21,
                                3,
                                "BOT"));

        when(catalogoPublicoService.obtenerSalon(8))
                .thenReturn(
                        new SalonPublicoResponse(
                                8,
                                3,
                                "Salon Fiesta",
                                "Salon activo",
                                40));

        when(disponibilidadSalonService.consultar(
                8,
                input.fecha(),
                input.horaInicio(),
                input.horaFin()))
                .thenReturn(
                        new DisponibilidadSalonResponse(
                                8,
                                input.fecha(),
                                input.horaInicio(),
                                input.horaFin(),
                                true,
                                "Disponible"));

        AiAvailabilityResult result =
                tool.ejecutar(
                        context,
                        input);

        assertEquals(21, result.idConversacion());
        assertEquals(3, result.idSede());
        assertEquals(8, result.idSalon());
        assertEquals("Salon Fiesta", result.nombreSalon());
        assertEquals(40, result.capacidad());
        assertTrue(result.disponible());
        assertEquals("Disponible", result.motivo());

        verify(chatClienteService)
                .obtenerPropia(
                        "cliente@bembos.test",
                        21);

        verify(catalogoPublicoService)
                .obtenerSalon(8);

        verify(disponibilidadSalonService)
                .consultar(
                        8,
                        input.fecha(),
                        input.horaInicio(),
                        input.horaFin());
    }

    @Test
    void conversacionSinSedePuedeConsultarSalonActivoSinMutarContexto() {
        AiAvailabilityTool tool =
                tool();

        AiAvailabilityInput input =
                input();

        when(chatClienteService.obtenerPropia(
                "cliente@bembos.test",
                31))
                .thenReturn(
                        conversacion(
                                31,
                                null,
                                "BOT"));

        when(catalogoPublicoService.obtenerSalon(8))
                .thenReturn(
                        new SalonPublicoResponse(
                                8,
                                5,
                                "Salon Libre",
                                "Salon activo",
                                25));

        when(disponibilidadSalonService.consultar(
                8,
                input.fecha(),
                input.horaInicio(),
                input.horaFin()))
                .thenReturn(
                        new DisponibilidadSalonResponse(
                                8,
                                input.fecha(),
                                input.horaInicio(),
                                input.horaFin(),
                                false,
                                "Horario ocupado"));

        AiAvailabilityResult result =
                tool.ejecutar(
                        new AiToolContext(
                                "cliente@bembos.test",
                                31),
                        input);

        assertEquals(5, result.idSede());
        assertFalse(result.disponible());
        assertEquals(
                "Horario ocupado",
                result.motivo());
    }

    @Test
    void rechazaSalonDeOtraSedeAntesDeConsultarDisponibilidad() {
        AiAvailabilityTool tool =
                tool();

        AiAvailabilityInput input =
                input();

        when(chatClienteService.obtenerPropia(
                "cliente@bembos.test",
                41))
                .thenReturn(
                        conversacion(
                                41,
                                3,
                                "BOT"));

        when(catalogoPublicoService.obtenerSalon(8))
                .thenReturn(
                        new SalonPublicoResponse(
                                8,
                                9,
                                "Salon Ajeno",
                                "Otra sede",
                                30));

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> tool.ejecutar(
                                new AiToolContext(
                                        "cliente@bembos.test",
                                        41),
                                input));

        assertTrue(
                error.getMessage()
                        .contains("sede"));

        verify(disponibilidadSalonService, never())
                .consultar(
                        8,
                        input.fecha(),
                        input.horaInicio(),
                        input.horaFin());
    }

    @Test
    void noEjecutaToolCuandoConversacionYaEstaEscalada() {
        AiAvailabilityTool tool =
                tool();

        AiAvailabilityInput input =
                input();

        when(chatClienteService.obtenerPropia(
                "cliente@bembos.test",
                51))
                .thenReturn(
                        conversacion(
                                51,
                                3,
                                "EN_ATENCION"));

        assertThrows(
                IllegalStateException.class,
                () -> tool.ejecutar(
                        new AiToolContext(
                                "cliente@bembos.test",
                                51),
                        input));

        verify(catalogoPublicoService, never())
                .obtenerSalon(8);

        verify(disponibilidadSalonService, never())
                .consultar(
                        8,
                        input.fecha(),
                        input.horaInicio(),
                        input.horaFin());
    }

    private AiAvailabilityTool tool() {
        return new AiAvailabilityTool(
                chatClienteService,
                catalogoPublicoService,
                disponibilidadSalonService);
    }

    private AiAvailabilityInput input() {
        return new AiAvailabilityInput(
                8,
                LocalDate.of(
                        2026,
                        11,
                        15),
                LocalTime.of(
                        16,
                        0),
                LocalTime.of(
                        18,
                        0));
    }

    private ConversacionDetalleResponse conversacion(
            Integer idConversacion,
            Integer idSede,
            String estado) {

        return new ConversacionDetalleResponse(
                idConversacion,
                7,
                idSede,
                estado,
                null,
                null,
                null,
                List.of());
    }
}