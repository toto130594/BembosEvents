package com.bembosevents.controller;

import com.bembosevents.dto.auth.EmailVerificationRequest;
import com.bembosevents.dto.auth.EmailVerificationTokenRequest;
import com.bembosevents.dto.common.MensajeResponse;
import com.bembosevents.service.EmailVerificationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/correo")
public class EmailVerificationController {

    private static final String GENERIC_REQUEST_MESSAGE =
            "Si el correo requiere verificacion, se enviaran instrucciones";

    private final EmailVerificationService
            emailVerificationService;

    public EmailVerificationController(
            EmailVerificationService emailVerificationService) {

        this.emailVerificationService =
                emailVerificationService;
    }

    @PostMapping("/reenviar")
    public ResponseEntity<MensajeResponse> reenviar(
            @Valid @RequestBody
            EmailVerificationRequest request) {

        emailVerificationService.solicitarReenvio(
                request.correo());

        return ResponseEntity.ok(
                new MensajeResponse(
                        GENERIC_REQUEST_MESSAGE));
    }

    @PostMapping("/verificar")
    public ResponseEntity<MensajeResponse> verificar(
            @Valid @RequestBody
            EmailVerificationTokenRequest request) {

        emailVerificationService.verificar(
                request.token());

        return ResponseEntity.ok(
                new MensajeResponse(
                        "Correo verificado correctamente"));
    }
}
