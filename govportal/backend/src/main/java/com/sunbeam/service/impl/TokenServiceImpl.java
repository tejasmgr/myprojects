package com.sunbeam.service.impl;

import com.sunbeam.exception.InvalidTokenException;
import com.sunbeam.model.Token;
import com.sunbeam.model.User;
import com.sunbeam.repository.TokenRepository;
import com.sunbeam.service.TokenService;
import lombok.RequiredArgsConstructor;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final TokenRepository tokenRepository;
    @Autowired
    ModelMapper mapper;
    
    @Override
    public void createVerificationToken(User user, String token) {
        Token verificationToken = new Token();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationToken.setType(Token.TokenType.VERIFICATION);
        verificationToken.setExpiryDate(LocalDateTime.now().plusHours(24));
        tokenRepository.save(verificationToken);
    }

    @Override
    public void createPasswordResetToken(User user, String token) {
        Token resetToken = new Token();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setType(Token.TokenType.PASSWORD_RESET);
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(2));
        tokenRepository.save(resetToken);
    }

    @Override
    public Token validateToken(String tokenValue) {
        Token token = tokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new InvalidTokenException("Invalid token"));
        
        if(token.getExpiryDate().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(token);
            throw new InvalidTokenException("Token expired");
        }
        
        return token;
    }
}