package com.bembosevents.controller;

import com.bembosevents.dto.mfa.MfaActivateRequest;
import com.bembosevents.dto.mfa.MfaActivateResponse;
import com.bembosevents.dto.mfa.MfaSetupResponse;
import com.bembosevents.service.MfaEnrollmentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mfa")
public class MfaController {

    private final MfaEnrollmentService
            mfaEnrollmentService;

    public MfaController(
            MfaEnrollmentService mfaEnrollmentService) {

        this.mfaEnrollmentService =
                mfaEnrollmentService;
    }

    @PostMapping("/setup")
    public ResponseEntity<MfaSetupResponse> setup(
            Authentication authentication) {

        String correo =
                authenticatedUsername(
                        authentication);

        return ResponseEntity.ok(
                mfaEnrollmentService.setup(
                        correo));
    }

    @PostMapping("/activate")
    public ResponseEntity<MfaActivateResponse> activate(
            Authentication authentication,
            @Valid @RequestBody
            MfaActivateRequest request) {

        String correo =
                authenticatedUsername(
                        authentication);

        return ResponseEntity.ok(
                mfaEnrollmentService.activate(
                        correo,
                        request.code()));
    }

    private String authenticatedUsername(
            Authentication authentication) {

        if (authentication == null
                || authentication instanceof
                AnonymousAuthenticationToken
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || authentication.getName().isBlank()) {

            throw new BadCredentialsException(
                    "Autenticacion requerida");
        }

        return authentication.getName();
    }
}