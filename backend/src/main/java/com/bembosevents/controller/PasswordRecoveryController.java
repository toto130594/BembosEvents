package com.bembosevents.controller;

import com.bembosevents.dto.auth.PasswordRecoveryRequest;
import com.bembosevents.dto.auth.PasswordResetRequest;
import com.bembosevents.dto.common.MensajeResponse;
import com.bembosevents.service.PasswordRecoveryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/password")
public class PasswordRecoveryController {

    private static final String GENERIC_REQUEST_MESSAGE =
            "Si el correo existe, se enviaran instrucciones para restablecer la contrasena";

    private final PasswordRecoveryService
            passwordRecoveryService;

    public PasswordRecoveryController(
            PasswordRecoveryService passwordRecoveryService) {

        this.passwordRecoveryService =
                passwordRecoveryService;
    }

    @PostMapping("/solicitar")
    public ResponseEntity<MensajeResponse> solicitar(
            @Valid @RequestBody
            PasswordRecoveryRequest request) {

        passwordRecoveryService.solicitar(
                request.correo());

        return ResponseEntity.ok(
                new MensajeResponse(
                        GENERIC_REQUEST_MESSAGE));
    }

    @PostMapping("/restablecer")
    public ResponseEntity<MensajeResponse> restablecer(
            @Valid @RequestBody
            PasswordResetRequest request) {

        passwordRecoveryService.restablecer(
                request.token(),
                request.nuevaPassword());

        return ResponseEntity.ok(
                new MensajeResponse(
                        "Contrasena restablecida correctamente"));
    }
}
