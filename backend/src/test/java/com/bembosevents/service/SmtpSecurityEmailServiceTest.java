package com.bembosevents.service;

import com.bembosevents.service.impl.SmtpSecurityEmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SmtpSecurityEmailServiceTest {

    @Mock
    private JavaMailSender javaMailSender;

    @Test
    void debeEnviarRecuperacionPassword() {

        SmtpSecurityEmailService service =
                new SmtpSecurityEmailService(
                        javaMailSender,
                        "no-reply@bembosevents.local");

        service.sendPasswordRecovery(
                "cliente@bembos.test",
                "raw-token-secreto");

        SimpleMailMessage message =
                capturarMensaje();

        assertEquals(
                "no-reply@bembosevents.local",
                message.getFrom());

        assertArrayEquals(
                new String[] {
                        "cliente@bembos.test"
                },
                message.getTo());

        assertEquals(
                "Bembos Events - Recuperacion de contrasena",
                message.getSubject());

        assertTrue(
                message.getText()
                        .contains(
                                "BEMBOS_PASSWORD_RECOVERY"));

        assertTrue(
                message.getText()
                        .contains(
                                "TOKEN: raw-token-secreto"));
    }

    @Test
    void debeEnviarVerificacionCorreo() {

        SmtpSecurityEmailService service =
                new SmtpSecurityEmailService(
                        javaMailSender,
                        "no-reply@bembosevents.local");

        service.sendEmailVerification(
                "cliente@bembos.test",
                "raw-token-secreto");

        SimpleMailMessage message =
                capturarMensaje();

        assertEquals(
                "Bembos Events - Verificacion de correo",
                message.getSubject());

        assertTrue(
                message.getText()
                        .contains(
                                "BEMBOS_EMAIL_VERIFICATION"));

        assertTrue(
                message.getText()
                        .contains(
                                "TOKEN: raw-token-secreto"));
    }

    private SimpleMailMessage capturarMensaje() {

        ArgumentCaptor<SimpleMailMessage> captor =
                ArgumentCaptor.forClass(
                        SimpleMailMessage.class);

        verify(javaMailSender)
                .send(captor.capture());

        return captor.getValue();
    }
}
