package com.sunbeam.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;

import com.sunbeam.dto.request.ChangePasswordRequest;
import com.sunbeam.dto.request.LoginRequest;
import com.sunbeam.dto.request.RegisterRequest;
import com.sunbeam.dto.request.ResetPasswordRequest;
import com.sunbeam.dto.response.AuthResponse;
import com.sunbeam.dto.response.ErrorResponse;
import com.sunbeam.exception.AccountBlockedException;
import com.sunbeam.exception.AccountDisabledException;
import com.sunbeam.exception.BadCredentialsException;
import com.sunbeam.exception.DatabaseOperationException;
import com.sunbeam.exception.EmailAlreadyExistsException;
import com.sunbeam.exception.InvalidTokenException;
import com.sunbeam.exception.TokenExpiredException;
import com.sunbeam.exception.UserNotFoundException;
import com.sunbeam.service.AuthService;
import com.sunbeam.service.UserService;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
	
	

    private final AuthService authService;
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest request) {
    	try {
    		return new ResponseEntity<>(authService.registerUser(request), HttpStatus.CREATED);
		} catch (EmailAlreadyExistsException e) {
	        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("EMAIL_EXISTS", "Email is Already Registered"));
	    }
    	catch (DatabaseOperationException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("DATABASE_ERROR", "Database error occurred. Please try again."));
		}
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest request) {
      AuthResponse response = authService.authenticateUser(request);
        return ResponseEntity.ok(response);
        
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
    	try {
            authService.initiatePasswordReset(email);
            return ResponseEntity.ok("Password reset link sent to your registered email");
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User  not found");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred");
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @RequestParam String token,
            @RequestBody String request
    ) {
    	try {
            authService.completePasswordReset(token, request);
            return ResponseEntity.ok("Password reset successfully");
        } catch (InvalidTokenException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid token");
        } catch (TokenExpiredException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token expired");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred");
        }
    }
    
    

    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
    	try {
            authService.verifyEmail(token);
            return ResponseEntity.ok("Email verified successfully");
        } catch (InvalidTokenException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid token");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred");
        }
    }
}