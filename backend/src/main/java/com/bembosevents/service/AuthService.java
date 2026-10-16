package com.bembosevents.service;

import com.bembosevents.dto.auth.AuthResponse;
import com.bembosevents.dto.auth.LoginRequest;
import com.bembosevents.dto.auth.RegistroRequest;

public interface AuthService {

    void registrar(RegistroRequest request);

    AuthResponse login(LoginRequest request);
}