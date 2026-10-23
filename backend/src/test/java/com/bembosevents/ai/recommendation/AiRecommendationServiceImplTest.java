package com.bembosevents.ai.recommendation;

import com.bembosevents.dto.catalogo.PaqueteResponse;
import com.bembosevents.dto.catalogo.ServicioAdicionalResponse;
import com.bembosevents.dto.catalogo.TematicaResponse;
import com.bembosevents.service.CatalogoPublicoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiRecommendationServiceImplTest {

    @Mock
    private CatalogoPublicoService catalogoPublicoService;

    @Test
    void ordenaPaquetesPorAfinidadDeInvitadosSinInventarCapacidad() {
        when(catalogoPublicoService.listarPaquetes())
                .thenReturn(
                        List.of(
                                paquete(
                                        1,
                                        "Basico",
                                        "Cumpleanos clasico",
                                        "300.00",
                                        20),
                                paquete(
                                        2,
                                        "Mediano",
                                        "Cumpleanos familiar",
                                        "450.00",
                                        30),
                                paquete(
                                        3,
                                        "Grande",
                                        "Evento amplio",
                                        "700.00",
                                        50)));

        AiRecommendationService service =
                new AiRecommendationServiceImpl(
                        catalogoPublicoService);

        AiRecommendationResult result =
                service.recomendar(
                        new AiRecommendationCriteria(
                                28,
                                "",
                                3));

        assertEquals(
                2,
                result.paquetes()
                        .get(0)
                        .idPaquete());

        assertEquals(
                2,
                result.paquetes()
                        .get(0)
                        .diferenciaPersonas());

        assertTrue(
                result.tematicas()
                        .isEmpty());

        assertTrue(
                result.serviciosAdicionales()
                        .isEmpty());
    }

    @Test
    void recomiendaTematicasYServiciosSoloCuandoCoincidenConPreferencias() {
        when(catalogoPublicoService.listarPaquetes())
                .thenReturn(
                        List.of(
                                paquete(
                                        1,
                                        "Cumpleanos",
                                        "Paquete de superheroes",
                                        "350.00",
                                        25)));

        when(catalogoPublicoService.listarTematicas())
                .thenReturn(
                        List.of(
                                new TematicaResponse(
                                        10,
                                        "Superheroes",
                                        "Decoracion de heroes y comic",
                                        new BigDecimal("80.00")),
                                new TematicaResponse(
                                        11,
                                        "Princesas",
                                        "Decoracion elegante",
                                        new BigDecimal("70.00"))));

        when(catalogoPublicoService.listarServiciosAdicionales())
                .thenReturn(
                        List.of(
                                new ServicioAdicionalResponse(
                                        20,
                                        "Animador de superheroes",
                                        "Personajes y juegos de heroes",
                                        new BigDecimal("120.00")),
                                new ServicioAdicionalResponse(
                                        21,
                                        "Fotografia",
                                        "Cobertura fotografica",
                                        new BigDecimal("100.00"))));

        AiRecommendationService service =
                new AiRecommendationServiceImpl(
                        catalogoPublicoService);

        AiRecommendationResult result =
                service.recomendar(
                        new AiRecommendationCriteria(
                                25,
                                "Quiero superheroes y heroes",
                                3));

        assertEquals(
                10,
                result.tematicas()
                        .get(0)
                        .idTematica());

        assertEquals(
                20,
                result.serviciosAdicionales()
                        .get(0)
                        .idServicioAdicional());

        assertEquals(
                1,
                result.tematicas()
                        .size());

        assertEquals(
                1,
                result.serviciosAdicionales()
                        .size());
    }

    @Test
    void limitaResultadosDeFormaDeterministica() {
        when(catalogoPublicoService.listarPaquetes())
                .thenReturn(
                        List.of(
                                paquete(
                                        1,
                                        "A",
                                        "",
                                        "100.00",
                                        10),
                                paquete(
                                        2,
                                        "B",
                                        "",
                                        "110.00",
                                        20),
                                paquete(
                                        3,
                                        "C",
                                        "",
                                        "120.00",
                                        30)));

        AiRecommendationService service =
                new AiRecommendationServiceImpl(
                        catalogoPublicoService);

        AiRecommendationResult result =
                service.recomendar(
                        new AiRecommendationCriteria(
                                22,
                                "",
                                2));

        assertEquals(
                2,
                result.paquetes()
                        .size());
    }

    private PaqueteResponse paquete(
            Integer id,
            String nombre,
            String descripcion,
            String precio,
            Integer personasBase) {

        return new PaqueteResponse(
                id,
                nombre,
                descripcion,
                new BigDecimal(precio),
                personasBase);
    }
}