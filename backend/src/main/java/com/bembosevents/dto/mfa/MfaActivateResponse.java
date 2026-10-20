package com.bembosevents.dto.mfa;

import java.util.List;

public record MfaActivateResponse(
        boolean mfaHabilitado,
        List<String> recoveryCodes) {

    public MfaActivateResponse {

        if (recoveryCodes == null) {
            throw new IllegalArgumentException(
                    "recoveryCodes requerido");
        }

        recoveryCodes =
                List.copyOf(
                        recoveryCodes);
    }
}