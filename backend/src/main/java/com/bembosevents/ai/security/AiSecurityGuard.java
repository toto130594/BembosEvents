package com.bembosevents.ai.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.time.Clock;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

@Component
public class AiSecurityGuard {

    private static final long CLEANUP_INTERVAL =
            256L;

    private static final String INJECTION_RESPONSE =
            "No puedo seguir instrucciones que intenten cambiar, revelar o eludir las reglas internas del asistente. "
                    + "Puedo ayudarte con una consulta valida de Bembos Events.";

    private static final String RATE_RESPONSE =
            "Has enviado varias solicitudes de IA en poco tiempo. Intenta nuevamente en unos momentos.";

    private static final String PROVIDER_BUDGET_RESPONSE =
            "El presupuesto temporal de conversacion general con IA fue alcanzado. "
                    + "Las consultas deterministicas de disponibilidad, catalogo, reserva y conocimiento siguen protegidas por el backend.";

    private static final String LENGTH_RESPONSE =
            "El mensaje es demasiado largo para ser procesado por el asistente. "
                    + "Resume la solicitud y vuelve a intentarlo.";

    private static final Pattern OVERRIDE_PATTERN =
            Pattern.compile(
                    "(ignore|ignora|olvida|omite).{0,50}"
                            + "(instrucciones|reglas|system prompt|prompt del sistema|developer|desarrollador)",
                    Pattern.CASE_INSENSITIVE);

    private static final Pattern DISCLOSURE_PATTERN =
            Pattern.compile(
                    "(muestra|revela|dime|imprime|show|reveal).{0,50}"
                            + "(system prompt|prompt del sistema|instrucciones internas|developer message|mensaje del desarrollador)",
                    Pattern.CASE_INSENSITIVE);

    private static final Pattern EXECUTION_PATTERN =
            Pattern.compile(
                    "(ejecuta|execute|corre|run).{0,40}"
                            + "(powershell|cmd|bash|shell|sql|filesystem|sistema de archivos)",
                    Pattern.CASE_INSENSITIVE);

    private static final Pattern TOOL_BYPASS_PATTERN =
            Pattern.compile(
                    "(usa|llama|invoca|call).{0,40}"
                            + "(tool|herramienta).{0,40}"
                            + "(arbitraria|cualquiera|oculta|no permitida|forbidden|hidden)",
                    Pattern.CASE_INSENSITIVE);

    private final boolean enabled;
    private final int maxMessageLength;
    private final int turnMaxRequests;
    private final Duration turnWindow;
    private final int providerMaxRequests;
    private final Duration providerWindow;
    private final Clock clock;

    private final ConcurrentHashMap<String, WindowCounter>
            turnWindows =
            new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, WindowCounter>
            providerWindows =
            new ConcurrentHashMap<>();

    private final AtomicLong operations =
            new AtomicLong();

    @Autowired

    public AiSecurityGuard(
            @Value("${app.chat.ai.security.enabled:true}")
            boolean enabled,
            @Value("${app.chat.ai.security.max-message-length:2000}")
            int maxMessageLength,
            @Value("${app.chat.ai.security.turn.max-requests:20}")
            int turnMaxRequests,
            @Value("${app.chat.ai.security.turn.window-seconds:60}")
            long turnWindowSeconds,
            @Value("${app.chat.ai.security.provider.max-requests:30}")
            int providerMaxRequests,
            @Value("${app.chat.ai.security.provider.window-seconds:3600}")
            long providerWindowSeconds) {

        this(
                enabled,
                maxMessageLength,
                turnMaxRequests,
                turnWindowSeconds,
                providerMaxRequests,
                providerWindowSeconds,
                Clock.systemUTC());
    }

    AiSecurityGuard(
            boolean enabled,
            int maxMessageLength,
            int turnMaxRequests,
            long turnWindowSeconds,
            int providerMaxRequests,
            long providerWindowSeconds,
            Clock clock) {

        if (maxMessageLength < 100) {
            throw new IllegalArgumentException(
                    "maxMessageLength debe ser al menos 100");
        }

        if (turnMaxRequests < 1
                || providerMaxRequests < 1) {
            throw new IllegalArgumentException(
                    "Los limites de solicitudes deben ser mayores que cero");
        }

        if (turnWindowSeconds < 1
                || providerWindowSeconds < 1) {
            throw new IllegalArgumentException(
                    "Las ventanas deben ser mayores que cero");
        }

        if (clock == null) {
            throw new IllegalArgumentException(
                    "clock es obligatorio");
        }

        this.enabled =
                enabled;

        this.maxMessageLength =
                maxMessageLength;

        this.turnMaxRequests =
                turnMaxRequests;

        this.turnWindow =
                Duration.ofSeconds(
                        turnWindowSeconds);

        this.providerMaxRequests =
                providerMaxRequests;

        this.providerWindow =
                Duration.ofSeconds(
                        providerWindowSeconds);

        this.clock =
                clock;
    }

    public AiSecurityDecision evaluarTurno(
            String correoCliente,
            Integer idConversacion,
            String mensajeCliente) {

        validarIdentidad(
                correoCliente,
                idConversacion);

        if (mensajeCliente == null
                || mensajeCliente.isBlank()) {
            throw new IllegalArgumentException(
                    "El mensaje del cliente es obligatorio");
        }

        if (mensajeCliente.length()
                > maxMessageLength) {

            return AiSecurityDecision.bloquear(
                    "MESSAGE_TOO_LONG",
                    LENGTH_RESPONSE);
        }

        String normalized =
                normalizar(
                        mensajeCliente);

        if (esPromptInjection(
                normalized)) {

            return AiSecurityDecision.bloquear(
                    "PROMPT_INJECTION_BLOCKED",
                    INJECTION_RESPONSE);
        }

        if (!enabled) {
            return AiSecurityDecision.permitir();
        }

        String key =
                hashSubject(
                        correoCliente,
                        idConversacion,
                        "turn");

        if (!allow(
                turnWindows,
                key,
                turnMaxRequests,
                turnWindow)) {

            return AiSecurityDecision.bloquear(
                    "TURN_RATE_LIMIT",
                    RATE_RESPONSE);
        }

        cleanupSometimes();

        return AiSecurityDecision.permitir();
    }

    public AiSecurityDecision evaluarPresupuestoProvider(
            String correoCliente,
            Integer idConversacion) {

        validarIdentidad(
                correoCliente,
                idConversacion);

        if (!enabled) {
            return AiSecurityDecision.permitir();
        }

        String key =
                hashSubject(
                        correoCliente,
                        idConversacion,
                        "provider");

        if (!allow(
                providerWindows,
                key,
                providerMaxRequests,
                providerWindow)) {

            return AiSecurityDecision.bloquear(
                    "PROVIDER_BUDGET_LIMIT",
                    PROVIDER_BUDGET_RESPONSE);
        }

        cleanupSometimes();

        return AiSecurityDecision.permitir();
    }

    private boolean esPromptInjection(
            String normalized) {

        return OVERRIDE_PATTERN
                        .matcher(
                                normalized)
                        .find()
                || DISCLOSURE_PATTERN
                        .matcher(
                                normalized)
                        .find()
                || EXECUTION_PATTERN
                        .matcher(
                                normalized)
                        .find()
                || TOOL_BYPASS_PATTERN
                        .matcher(
                                normalized)
                        .find();
    }

    private boolean allow(
            ConcurrentHashMap<String, WindowCounter> windows,
            String key,
            int maxRequests,
            Duration window) {

        long now =
                clock.millis();

        long windowMillis =
                window.toMillis();

        DecisionHolder holder =
                new DecisionHolder();

        windows.compute(
                key,
                (ignored, current) -> {
                    if (current == null
                            || now - current.windowStartMillis
                                    >= windowMillis) {

                        holder.allowed =
                                true;

                        return new WindowCounter(
                                now,
                                1);
                    }

                    if (current.count
                            >= maxRequests) {

                        holder.allowed =
                                false;

                        return current;
                    }

                    holder.allowed =
                            true;

                    return new WindowCounter(
                            current.windowStartMillis,
                            current.count + 1);
                });

        return holder.allowed;
    }

    private void cleanupSometimes() {
        long current =
                operations.incrementAndGet();

        if (current % CLEANUP_INTERVAL != 0) {
            return;
        }

        cleanup(
                turnWindows,
                turnWindow);

        cleanup(
                providerWindows,
                providerWindow);
    }

    private void cleanup(
            ConcurrentHashMap<String, WindowCounter> windows,
            Duration window) {

        long now =
                clock.millis();

        long ttl =
                window.toMillis() * 2L;

        windows.entrySet()
                .removeIf(entry ->
                        now
                                - entry.getValue()
                                        .windowStartMillis
                                >= ttl);
    }

    private void validarIdentidad(
            String correoCliente,
            Integer idConversacion) {

        if (correoCliente == null
                || correoCliente.isBlank()) {
            throw new IllegalArgumentException(
                    "El correo del cliente es obligatorio");
        }

        if (idConversacion == null
                || idConversacion <= 0) {
            throw new IllegalArgumentException(
                    "La conversacion es obligatoria");
        }
    }

    private String hashSubject(
            String correoCliente,
            Integer idConversacion,
            String scope) {

        String material =
                correoCliente
                        .strip()
                        .toLowerCase(
                                Locale.ROOT)
                        + "|"
                        + idConversacion
                        + "|"
                        + scope;

        try {
            MessageDigest digest =
                    MessageDigest.getInstance(
                            "SHA-256");

            return HexFormat.of()
                    .formatHex(
                            digest.digest(
                                    material.getBytes(
                                            StandardCharsets.UTF_8)));
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(
                    "SHA-256 no disponible",
                    ex);
        }
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
                .replace('\r', ' ')
                .replace('\n', ' ')
                .strip();
    }

    private static final class DecisionHolder {
        private boolean allowed;
    }

    private record WindowCounter(
            long windowStartMillis,
            int count) {
    }
}