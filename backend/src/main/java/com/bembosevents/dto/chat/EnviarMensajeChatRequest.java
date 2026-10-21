package com.bembosevents.dto.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EnviarMensajeChatRequest(

        @NotBlank(message = "El mensaje es obligatorio")
        @Size(
                max = 4000,
                message = "El mensaje no puede superar 4000 caracteres"
        )
        String contenido
) {
}