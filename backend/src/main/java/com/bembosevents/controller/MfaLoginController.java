package com.bembosevents.controller;

import com.bembosevents.dto.auth.AuthResponse;
import com.bembosevents.dto.mfa.MfaLoginVerifyRequest;
import com.bembosevents.dto.mfa.MfaRecoveryLoginRequest;
import com.bembosevents.service.MfaLoginVerificationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/mfa")
public class MfaLoginController {

    private final MfaLoginVerificationService
            mfaLoginVerificationService;

    public MfaLoginController(
            MfaLoginVerificationService
                    mfaLoginVerificationService) {

        this.mfaLoginVerificationService =
                mfaLoginVerificationService;
    }

    @PostMapping("/verificar")
    public ResponseEntity<AuthResponse> verificar(
            @Valid
            @RequestBody
            MfaLoginVerifyRequest request) {

        return ResponseEntity.ok(
                mfaLoginVerificationService.verifyTotp(
                        request.challengeToken(),
                        request.code()));
    }

    @PostMapping("/recuperacion")
    public ResponseEntity<AuthResponse> recuperacion(
            @Valid
            @RequestBody
            MfaRecoveryLoginRequest request) {

        return ResponseEntity.ok(
                mfaLoginVerificationService.verifyRecovery(
                        request.challengeToken(),
                        request.recoveryCode()));
    }
}