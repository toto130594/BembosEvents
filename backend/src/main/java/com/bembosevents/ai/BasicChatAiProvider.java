package com.bembosevents.ai;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@ConditionalOnProperty(
        name = "app.chat.ai.provider",
        havingValue = "basic",
        matchIfMissing = true
)
public class BasicChatAiProvider
        implements ChatAiProvider {

    @Override
    public String responder(
            ChatAiRequest request) {

        if (request == null
                || request.mensajeCliente() == null
                || request.mensajeCliente().isBlank()) {

            throw new IllegalArgumentException(
                    "El mensaje del cliente es obligatorio");
        }

        String mensaje =
                request.mensajeCliente()
                        .strip()
                        .toLowerCase(Locale.ROOT);

        if (mensaje.matches(
                ".*\\b(hola|buenas|buenos dias|buenas tardes|buenas noches)\\b.*")) {

            return "Hola. Soy el asistente virtual de Bembos Events. "
                    + "Puedo ayudarte con preguntas generales sobre tu evento.";
        }

        if (mensaje.contains("ayuda")
                || mensaje.contains("que puedes hacer")
                || mensaje.contains("qué puedes hacer")) {

            return "Puedo orientarte sobre Bembos Events y mantener la conversacion. "
                    + "El flujo conversacional puede consultar disponibilidad, recomendar "
                    + "opciones, preparar un borrador de reserva y consultar informacion "
                    + "verificada cuando el orquestador detecta esos pedidos.";
        }

        return "Gracias por tu mensaje. Puedo ayudarte con informacion general "
                + "de Bembos Events. Si necesitas una gestion especifica, "
                + "cuentame un poco mas.";
    }
}