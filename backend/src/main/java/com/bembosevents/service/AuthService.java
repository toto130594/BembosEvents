package com.bembosevents.service;

import com.bembosevents.dto.auth.AuthResponse;
import com.bembosevents.dto.auth.SesionResponse;
import com.bembosevents.dto.auth.LoginRequest;
import com.bembosevents.dto.auth.LogoutRequest;
import com.bembosevents.dto.auth.RefreshRequest;
import com.bembosevents.dto.auth.RegistroRequest;

import java.util.List;

public interface AuthService {

    void registrar(RegistroRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refresh(RefreshRequest request);

    void logout(LogoutRequest request);

    void logoutAll(String correo);

    List<SesionResponse> listarSesiones(
            String correo);

    void revocarSesion(
            String correo,
            Long idSesionUsuario);
}
