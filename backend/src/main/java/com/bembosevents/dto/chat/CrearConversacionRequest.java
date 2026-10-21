package com.bembosevents.dto.chat;

import jakarta.validation.constraints.Positive;

public record CrearConversacionRequest(

        @Positive(message = "El id de sede debe ser positivo")
        Integer idSede
) {
}