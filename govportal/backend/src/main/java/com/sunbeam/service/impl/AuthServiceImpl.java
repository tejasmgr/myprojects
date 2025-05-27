package com.sunbeam.service.impl;

import com.sunbeam.dto.request.*;
import com.sunbeam.dto.response.AuthResponse;
import com.sunbeam.exception.*;
import com.sunbeam.model.*;
import com.sunbeam.repository.TokenRepository;
import com.sunbeam.repository.UserRepository;
import com.sunbeam.security.JwtUtil;
import com.sunbeam.service.AuthService;
import com.sunbeam.service.EmailService;
import com.sunbeam.service.TokenService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final ModelMapper modelMapper;
    private final EmailService emailService;
    private final TokenService tokenService;
    private final TokenRepository tokenRepository;
    private final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    @Override
    @Transactional
    public AuthResponse registerUser(RegisterRequest request) {
       if(userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already registered: "+request.getEmail());
        }
        User user = modelMapper.map(request, User.class);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(false);
        user.setRole(User.Role.CITIZEN);
        
        User savedUser = userRepository.save(user);

        String verificationToken = UUID.randomUUID().toString();
        tokenService.createVerificationToken(savedUser, verificationToken);
        emailService.sendVerificationEmail(user.getEmail(), verificationToken);
        
        Map<String, Object> extraClaimsRegister = new HashMap<>();
        extraClaimsRegister.put("roles", List.of(savedUser.getRole().name()));

        return AuthResponse.builder()
                .accessToken(jwtUtil.generateToken(extraClaimsRegister, new CustomUserDetails(savedUser)))
                .user(savedUser)
                .build();
    }

    @Override
    public AuthResponse authenticateUser(LoginRequest request) {
        try {
        	Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();
            
            
            if(!userDetails.isEnabled()) throw new AccountDisabledException("Account not verified");
            if(userDetails.isBlocked()) throw new AccountBlockedException("Account blocked");
            
            // Add roles to the JWT claims
            Map<String, Object> extraClaims = new HashMap<>();
            extraClaims.put("roles", List.of(user.getRole().name()));

            return AuthResponse.builder()
                    .accessToken(jwtUtil.generateToken(extraClaims, userDetails))
                    .user(user)
                    .build();
		} catch (BadCredentialsException e) {
			throw new BadCredentialsException("Invalid Username or Password");
		}catch (AccountDisabledException e) {
			throw new AccountDisabledException("Account not verified");
		}catch (AccountBlockedException e) {
			throw new AccountBlockedException("Account is blocked");
		}
    }

    @Override
    public void initiatePasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email : "+ email));
        
        String resetToken = UUID.randomUUID().toString();
        tokenService.createPasswordResetToken(user, resetToken);
        emailService.sendPasswordResetEmail(email, resetToken);
        logger.info("Password reset initiated for user: {}", email);
    }

    @Override
    @Transactional
    public void completePasswordReset(String token, String newPassword) {
        Token resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid token"));
        
        if(resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
        	tokenRepository.delete(resetToken);
            throw new TokenExpiredException("Token expired");
        }
        
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        tokenRepository.delete(resetToken);
        logger.info("Password reset completed for user: {}", user.getEmail());
    }

    @Override
    @Transactional
    public void verifyEmail(String token) {
    	logger.info("Attempting to verify email with token: {}", token);
        Token verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid token"));
        logger.info("Found token for user: {}", verificationToken.getUser().getEmail());
        
        
        User user = verificationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);
        tokenRepository.delete(verificationToken);
        
        logger.info("Email verified successfully for user: {}", user.getEmail());
    }

	
}