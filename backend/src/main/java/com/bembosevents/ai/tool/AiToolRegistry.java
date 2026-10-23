package com.bembosevents.ai.tool;

import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class AiToolRegistry {

    private final Map<AiToolType, AiTool<?, ?>> tools;

    public AiToolRegistry(
            List<AiTool<?, ?>> tools) {

        EnumMap<AiToolType, AiTool<?, ?>> byType =
                new EnumMap<>(AiToolType.class);

        for (AiTool<?, ?> tool : tools) {
            if (tool == null) {
                throw new IllegalArgumentException(
                        "La lista de AI tools no puede contener null");
            }

            AiTool<?, ?> previous =
                    byType.put(
                            tool.type(),
                            tool);

            if (previous != null) {
                throw new IllegalStateException(
                        "Existe mas de un AI tool para "
                                + tool.type().name());
            }
        }

        this.tools =
                Map.copyOf(byType);
    }

    public Set<AiToolType> tiposPermitidos() {
        return tools.keySet();
    }

    public AiTool<?, ?> exigir(
            AiToolType type) {

        if (type == null) {
            throw new IllegalArgumentException(
                    "El tipo de AI tool es obligatorio");
        }

        AiTool<?, ?> tool =
                tools.get(type);

        if (tool == null) {
            throw new IllegalArgumentException(
                    "AI tool no permitido: "
                            + type.name());
        }

        return tool;
    }
}