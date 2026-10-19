package com.bembosevents.service;

public interface SecurityEmailService {

    void sendPasswordRecovery(
            String correo,
            String rawToken
    );

    void sendEmailVerification(
            String correo,
            String rawToken
    );
}
