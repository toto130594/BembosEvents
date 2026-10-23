package com.bembosevents.ai.security;

public record AiSecurityDecision(
        boolean permitido,
        String codigo,
        String respuestaSegura) {

    public AiSecurityDecision {
        if (codigo == null
                || codigo.isBlank()) {
            throw new IllegalArgumentException(
                    "El codigo de decision es obligatorio");
        }

        if (permitido) {
            respuestaSegura = null;
        }
        else if (respuestaSegura == null
                || respuestaSegura.isBlank()) {
            throw new IllegalArgumentException(
                    "La respuesta segura es obligatoria al bloquear");
        }
    }

    public static AiSecurityDecision permitir() {
        return new AiSecurityDecision(
                true,
                "ALLOWED",
                null);
    }

    public static AiSecurityDecision bloquear(
            String codigo,
            String respuestaSegura) {

        return new AiSecurityDecision(
                false,
                codigo,
                respuestaSegura);
    }
}