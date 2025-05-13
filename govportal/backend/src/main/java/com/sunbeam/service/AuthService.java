package com.sunbeam.service;

import com.sunbeam.dto.request.LoginRequest;
import com.sunbeam.dto.request.RegisterRequest;
import com.sunbeam.dto.request.ResetPasswordRequest;
import com.sunbeam.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse registerUser(RegisterRequest request);
    AuthResponse authenticateUser(LoginRequest request);
    void initiatePasswordReset(String email);
    void completePasswordReset(String token, ResetPasswordRequest request);
    void verifyEmail(String token);
}