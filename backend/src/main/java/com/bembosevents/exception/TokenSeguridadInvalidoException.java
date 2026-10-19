package com.bembosevents.exception;

public class TokenSeguridadInvalidoException
        extends IllegalArgumentException {

    public TokenSeguridadInvalidoException() {
        super("Token invalido o expirado");
    }
}
