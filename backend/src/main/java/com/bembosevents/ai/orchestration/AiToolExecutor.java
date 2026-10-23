package com.bembosevents.ai.orchestration;

import com.bembosevents.ai.tool.AiTool;
import com.bembosevents.ai.tool.AiToolContext;
import com.bembosevents.ai.tool.AiToolRegistry;
import com.bembosevents.ai.tool.AiToolType;
import org.springframework.stereotype.Component;

@Component
public class AiToolExecutor {

    private final AiToolRegistry registry;

    public AiToolExecutor(
            AiToolRegistry registry) {

        this.registry =
                registry;
    }

    public <I, O> O ejecutar(
            AiToolType type,
            AiToolContext context,
            I input,
            Class<O> outputType) {

        if (type == null) {
            throw new IllegalArgumentException(
                    "El tipo de tool es obligatorio");
        }

        if (context == null) {
            throw new IllegalArgumentException(
                    "El contexto de tool es obligatorio");
        }

        if (input == null) {
            throw new IllegalArgumentException(
                    "El input del tool es obligatorio");
        }

        if (outputType == null) {
            throw new IllegalArgumentException(
                    "El tipo de salida es obligatorio");
        }

        AiTool<?, ?> tool =
                registry.exigir(
                        type);

        if (!tool.inputType()
                .isInstance(
                        input)) {

            throw new IllegalArgumentException(
                    "Input incompatible para AI tool "
                            + type.name());
        }

        Object output =
                ejecutarUnchecked(
                        tool,
                        context,
                        input);

        if (output == null) {
            throw new IllegalStateException(
                    "AI tool devolvio null: "
                            + type.name());
        }

        if (!outputType.isInstance(
                output)) {

            throw new IllegalStateException(
                    "Salida incompatible para AI tool "
                            + type.name());
        }

        return outputType.cast(
                output);
    }

    @SuppressWarnings({
            "rawtypes",
            "unchecked"
    })
    private Object ejecutarUnchecked(
            AiTool tool,
            AiToolContext context,
            Object input) {

        return tool.ejecutar(
                context,
                input);
    }
}