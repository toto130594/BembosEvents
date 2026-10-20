package com.bembosevents.controller;

import com.bembosevents.dto.auth.AuthResponse;
import com.bembosevents.dto.auth.SesionResponse;
import com.bembosevents.dto.auth.LoginRequest;
import com.bembosevents.dto.auth.LogoutRequest;
import com.bembosevents.dto.auth.RefreshRequest;
import com.bembosevents.dto.auth.RegistroRequest;
import com.bembosevents.dto.common.MensajeResponse;
import com.bembosevents.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

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

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @Valid @RequestBody RefreshRequest request) {

        return ResponseEntity.ok(
                authService.refresh(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @Valid @RequestBody LogoutRequest request) {

        authService.logout(
                request);

        return ResponseEntity
                .noContent()
                .build();
    }

    @PostMapping("/logout-all")
    public ResponseEntity<Void> logoutAll(
            Principal principal) {

        authService.logoutAll(
                principal == null
                        ? null
                        : principal.getName());

        return ResponseEntity
                .noContent()
                .build();
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<SesionResponse>> listarSesiones(
            Principal principal) {

        return ResponseEntity.ok(
                authService.listarSesiones(
                        principal == null
                                ? null
                                : principal.getName()));
    }

    @DeleteMapping("/sessions/{idSesionUsuario}")
    public ResponseEntity<Void> revocarSesion(
            @PathVariable Long idSesionUsuario,
            Principal principal) {

        authService.revocarSesion(
                principal == null
                        ? null
                        : principal.getName(),
                idSesionUsuario);

        return ResponseEntity
                .noContent()
                .build();
    }
}
