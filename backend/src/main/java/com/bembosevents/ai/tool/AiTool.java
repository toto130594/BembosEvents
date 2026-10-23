package com.bembosevents.ai.tool;

public interface AiTool<I, O> {

    AiToolType type();

    Class<I> inputType();

    O ejecutar(
            AiToolContext context,
            I input);
}