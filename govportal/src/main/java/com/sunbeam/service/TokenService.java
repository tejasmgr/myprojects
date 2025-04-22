package com.sunbeam.service;

import com.sunbeam.model.Token;
import com.sunbeam.model.User;

public interface TokenService {
    void createVerificationToken(User user, String token);
    void createPasswordResetToken(User user, String token);
    Token validateToken(String token);
}