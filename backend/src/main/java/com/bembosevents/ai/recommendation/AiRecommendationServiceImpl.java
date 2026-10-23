package com.bembosevents.ai.recommendation;

import com.bembosevents.dto.catalogo.PaqueteResponse;
import com.bembosevents.dto.catalogo.ServicioAdicionalResponse;
import com.bembosevents.dto.catalogo.TematicaResponse;
import com.bembosevents.service.CatalogoPublicoService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class AiRecommendationServiceImpl
        implements AiRecommendationService {

    private static final Set<String> STOP_WORDS =
            Set.of(
                    "para",
                    "con",
                    "una",
                    "uno",
                    "unos",
                    "unas",
                    "del",
                    "las",
                    "los",
                    "que",
                    "quiero",
                    "evento",
                    "fiesta");

    private final CatalogoPublicoService catalogoPublicoService;

    public AiRecommendationServiceImpl(
            CatalogoPublicoService catalogoPublicoService) {

        this.catalogoPublicoService =
                catalogoPublicoService;
    }

    @Override
    public AiRecommendationResult recomendar(
            AiRecommendationCriteria criteria) {

        if (criteria == null) {
            throw new IllegalArgumentException(
                    "Los criterios de recomendacion son obligatorios");
        }

        List<String> tokens =
                tokensPreferencia(
                        criteria.preferencias());

        List<AiPackageRecommendation> paquetes =
                catalogoPublicoService
                        .listarPaquetes()
                        .stream()
                        .filter(this::paqueteValido)
                        .map(paquete ->
                                mapPaquete(
                                        paquete,
                                        criteria.numeroInvitados(),
                                        tokens))
                        .sorted(
                                Comparator
                                        .comparing(
                                                AiPackageRecommendation::coincidenciasPreferencia,
                                                Comparator.reverseOrder())
                                        .thenComparing(
                                                AiPackageRecommendation::diferenciaPersonas)
                                        .thenComparing(
                                                AiPackageRecommendation::precioBase)
                                        .thenComparing(
                                                AiPackageRecommendation::idPaquete))
                        .limit(
                                criteria.maxResultados())
                        .toList();

        List<AiThemeRecommendation> tematicas =
                tokens.isEmpty()
                        ? List.of()
                        : catalogoPublicoService
                                .listarTematicas()
                                .stream()
                                .filter(this::tematicaValida)
                                .map(tematica ->
                                        mapTematica(
                                                tematica,
                                                tokens))
                                .filter(item ->
                                        item.coincidenciasPreferencia() > 0)
                                .sorted(
                                        Comparator
                                                .comparing(
                                                        AiThemeRecommendation::coincidenciasPreferencia,
                                                        Comparator.reverseOrder())
                                                .thenComparing(
                                                        AiThemeRecommendation::precioAdicional)
                                                .thenComparing(
                                                        AiThemeRecommendation::idTematica))
                                .limit(
                                        criteria.maxResultados())
                                .toList();

        List<AiServiceRecommendation> servicios =
                tokens.isEmpty()
                        ? List.of()
                        : catalogoPublicoService
                                .listarServiciosAdicionales()
                                .stream()
                                .filter(this::servicioValido)
                                .map(servicio ->
                                        mapServicio(
                                                servicio,
                                                tokens))
                                .filter(item ->
                                        item.coincidenciasPreferencia() > 0)
                                .sorted(
                                        Comparator
                                                .comparing(
                                                        AiServiceRecommendation::coincidenciasPreferencia,
                                                        Comparator.reverseOrder())
                                                .thenComparing(
                                                        AiServiceRecommendation::precioUnitario)
                                                .thenComparing(
                                                        AiServiceRecommendation::idServicioAdicional))
                                .limit(
                                        criteria.maxResultados())
                                .toList();

        String aviso =
                tokens.isEmpty()
                        ? "Los paquetes se ordenan por afinidad con el numero de invitados. "
                                + "Indica preferencias para recomendar tematicas y servicios. "
                                + "Los precios son de catalogo y no constituyen una cotizacion final."
                        : "Recomendacion referencial basada en catalogo activo, numero de invitados "
                                + "y coincidencias de preferencias. La reserva final vuelve a validar "
                                + "reglas, disponibilidad, cantidades y montos.";

        return new AiRecommendationResult(
                criteria.numeroInvitados(),
                criteria.preferencias(),
                paquetes,
                tematicas,
                servicios,
                aviso);
    }

    private AiPackageRecommendation mapPaquete(
            PaqueteResponse paquete,
            int numeroInvitados,
            List<String> tokens) {

        int diferencia =
                Math.abs(
                        paquete.cantidadPersonasBase()
                                - numeroInvitados);

        int coincidencias =
                contarCoincidencias(
                        paquete.nombre(),
                        paquete.descripcion(),
                        tokens);

        String motivo =
                coincidencias > 0
                        ? String.format(
                                Locale.ROOT,
                                "Coincide con %d preferencia(s). Cantidad base configurada: %d; diferencia frente al grupo: %d.",
                                coincidencias,
                                paquete.cantidadPersonasBase(),
                                diferencia)
                        : String.format(
                                Locale.ROOT,
                                "Cantidad base configurada: %d; diferencia frente al grupo: %d.",
                                paquete.cantidadPersonasBase(),
                                diferencia);

        return new AiPackageRecommendation(
                paquete.idPaquete(),
                paquete.nombre(),
                paquete.descripcion(),
                paquete.precioBase(),
                paquete.cantidadPersonasBase(),
                diferencia,
                coincidencias,
                motivo);
    }

    private AiThemeRecommendation mapTematica(
            TematicaResponse tematica,
            List<String> tokens) {

        int coincidencias =
                contarCoincidencias(
                        tematica.nombre(),
                        tematica.descripcion(),
                        tokens);

        return new AiThemeRecommendation(
                tematica.idTematica(),
                tematica.nombre(),
                tematica.descripcion(),
                tematica.precioAdicional(),
                coincidencias,
                String.format(
                        Locale.ROOT,
                        "Coincide con %d preferencia(s) expresada(s) por el cliente.",
                        coincidencias));
    }

    private AiServiceRecommendation mapServicio(
            ServicioAdicionalResponse servicio,
            List<String> tokens) {

        int coincidencias =
                contarCoincidencias(
                        servicio.nombre(),
                        servicio.descripcion(),
                        tokens);

        return new AiServiceRecommendation(
                servicio.idServicioAdicional(),
                servicio.nombre(),
                servicio.descripcion(),
                servicio.precio(),
                coincidencias,
                String.format(
                        Locale.ROOT,
                        "Coincide con %d preferencia(s) expresada(s) por el cliente.",
                        coincidencias));
    }

    private boolean paqueteValido(
            PaqueteResponse paquete) {

        return paquete != null
                && paquete.idPaquete() != null
                && paquete.nombre() != null
                && !paquete.nombre().isBlank()
                && paquete.precioBase() != null
                && paquete.precioBase().signum() >= 0
                && paquete.cantidadPersonasBase() != null
                && paquete.cantidadPersonasBase() > 0;
    }

    private boolean tematicaValida(
            TematicaResponse tematica) {

        return tematica != null
                && tematica.idTematica() != null
                && tematica.nombre() != null
                && !tematica.nombre().isBlank()
                && tematica.precioAdicional() != null
                && tematica.precioAdicional().signum() >= 0;
    }

    private boolean servicioValido(
            ServicioAdicionalResponse servicio) {

        return servicio != null
                && servicio.idServicioAdicional() != null
                && servicio.nombre() != null
                && !servicio.nombre().isBlank()
                && servicio.precio() != null
                && servicio.precio().signum() >= 0;
    }

    private int contarCoincidencias(
            String nombre,
            String descripcion,
            List<String> tokens) {

        if (tokens.isEmpty()) {
            return 0;
        }

        String texto =
                normalizar(
                        String.format(
                                Locale.ROOT,
                                "%s %s",
                                nombre == null ? "" : nombre,
                                descripcion == null ? "" : descripcion));

        int coincidencias = 0;

        for (String token : tokens) {
            if (texto.contains(token)) {
                coincidencias++;
            }
        }

        return coincidencias;
    }

    private List<String> tokensPreferencia(
            String preferencias) {

        String normalizado =
                normalizar(
                        preferencias == null
                                ? ""
                                : preferencias);

        if (normalizado.isBlank()) {
            return List.of();
        }

        return Arrays
                .stream(
                        normalizado.split("[^a-z0-9]+"))
                .map(String::strip)
                .filter(token ->
                        token.length() >= 3)
                .filter(token ->
                        !STOP_WORDS.contains(token))
                .distinct()
                .toList();
    }

    private String normalizar(
            String value) {

        String normalized =
                Normalizer.normalize(
                        value,
                        Normalizer.Form.NFD);

        return normalized
                .replaceAll(
                        "\\p{M}+",
                        "")
                .toLowerCase(
                        Locale.ROOT)
                .strip();
    }
}