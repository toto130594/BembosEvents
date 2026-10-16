package com.bembosevents.controller;

import com.bembosevents.dto.auth.AuthResponse;
import com.bembosevents.dto.auth.LoginRequest;
import com.bembosevents.dto.auth.RegistroRequest;
import com.bembosevents.dto.common.MensajeResponse;
import com.bembosevents.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(
            AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/registro")
    public ResponseEntity<MensajeResponse> registrar(
            @Valid @RequestBody RegistroRequest request) {

        authService.registrar(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        new MensajeResponse(
                                "Usuario registrado correctamente"));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(
                authService.login(request));
    }
}