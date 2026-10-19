package com.bembosevents.service.impl;

import com.bembosevents.service.SecurityEmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(
        name = "app.security.email.enabled",
        havingValue = "true")
public class SmtpSecurityEmailService
        implements SecurityEmailService {

    private static final String PASSWORD_RECOVERY_SUBJECT =
            "Bembos Events - Recuperacion de contrasena";

    private static final String EMAIL_VERIFICATION_SUBJECT =
            "Bembos Events - Verificacion de correo";

    private final JavaMailSender javaMailSender;

    private final String fromAddress;

    public SmtpSecurityEmailService(
            JavaMailSender javaMailSender,
            @Value("${app.security.email.from}")
            String fromAddress) {

        this.javaMailSender =
                javaMailSender;

        this.fromAddress =
                requireValue(
                        fromAddress,
                        "Remitente requerido");
    }

    @Override
    public void sendPasswordRecovery(
            String correo,
            String rawToken) {

        send(
                correo,
                PASSWORD_RECOVERY_SUBJECT,
                "BEMBOS_PASSWORD_RECOVERY",
                rawToken,
                "Utiliza este token para restablecer tu contrasena.");
    }

    @Override
    public void sendEmailVerification(
            String correo,
            String rawToken) {

        send(
                correo,
                EMAIL_VERIFICATION_SUBJECT,
                "BEMBOS_EMAIL_VERIFICATION",
                rawToken,
                "Utiliza este token para verificar tu correo.");
    }

    private void send(
            String correo,
            String subject,
            String marker,
            String rawToken,
            String instruction) {

        String recipient =
                requireValue(
                        correo,
                        "Correo requerido");

        String token =
                requireValue(
                        rawToken,
                        "Token requerido");

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setFrom(
                fromAddress);

        message.setTo(
                recipient);

        message.setSubject(
                subject);

        message.setText(
                marker
                        + "\n\n"
                        + instruction
                        + "\n\n"
                        + "TOKEN: "
                        + token
                        + "\n\n"
                        + "Si no solicitaste esta operacion, ignora este correo.");

        javaMailSender.send(
                message);
    }

    private String requireValue(
            String value,
            String message) {

        if (value == null
                || value.isBlank()) {

            throw new IllegalArgumentException(
                    message);
        }

        return value.trim();
    }
}
