package com.bembosevents.security.mfa;

public record MfaRecoveryCodeMaterial(
        String rawCode,
        String codeHash) {

    public MfaRecoveryCodeMaterial {

        if (rawCode == null
                || rawCode.isBlank()) {

            throw new IllegalArgumentException(
                    "rawCode requerido");
        }

        if (codeHash == null
                || !codeHash.matches(
                        "[0-9a-f]{64}")) {

            throw new IllegalArgumentException(
                    "codeHash SHA-256 invalido");
        }
    }
}
