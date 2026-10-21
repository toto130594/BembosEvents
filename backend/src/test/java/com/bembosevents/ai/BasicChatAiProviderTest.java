package com.bembosevents.ai;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BasicChatAiProviderTest {

    private final BasicChatAiProvider provider =
            new BasicChatAiProvider();

    @Test
    void respondeSaludoBasico() {

        String response =
                provider.responder(
                        new ChatAiRequest(
                                10,
                                null,
                                "Hola"
                        )
                );

        assertTrue(
                response.contains(
                        "Bembos Events")
        );
    }

    @Test
    void respondeConsultaGeneralSinInventarDatos() {

        String response =
                provider.responder(
                        new ChatAiRequest(
                                10,
                                2,
                                "Quiero ayuda con mi fiesta"
                        )
                );

        assertTrue(
                response.length() > 20
        );
    }
}