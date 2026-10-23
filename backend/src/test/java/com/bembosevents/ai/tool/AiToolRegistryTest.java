package com.bembosevents.ai.tool;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiToolRegistryTest {

    @Test
    void registraSoloToolsDeclarados() {
        StubTool tool =
                new StubTool();

        AiToolRegistry registry =
                new AiToolRegistry(
                        List.of(tool));

        assertEquals(
                1,
                registry.tiposPermitidos().size());

        assertTrue(
                registry.tiposPermitidos().contains(
                        AiToolType.CONSULTAR_DISPONIBILIDAD));

        assertSame(
                tool,
                registry.exigir(
                        AiToolType.CONSULTAR_DISPONIBILIDAD));
    }

    @Test
    void rechazaTiposDuplicados() {
        assertThrows(
                IllegalStateException.class,
                () -> new AiToolRegistry(
                        List.of(
                                new StubTool(),
                                new StubTool())));
    }

    private static final class StubTool
            implements AiTool<AiAvailabilityInput, AiAvailabilityResult> {

        @Override
        public AiToolType type() {
            return AiToolType.CONSULTAR_DISPONIBILIDAD;
        }

        @Override
        public Class<AiAvailabilityInput> inputType() {
            return AiAvailabilityInput.class;
        }

        @Override
        public AiAvailabilityResult ejecutar(
                AiToolContext context,
                AiAvailabilityInput input) {

            return null;
        }
    }
}