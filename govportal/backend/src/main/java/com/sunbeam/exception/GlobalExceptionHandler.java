package com.sunbeam.exception;

import com.sunbeam.dto.response.ErrorResponse;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage()));
        
        return new ResponseEntity<>(
            new ErrorResponse("VALIDATION_ERROR", "Validation failed", errors),
            HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String field = violation.getPropertyPath().toString();
            errors.put(field, violation.getMessage());
        });
        return new ResponseEntity<>(
            new ErrorResponse("CONSTRAINT_VIOLATION", "Data constraint violation", errors),
            HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler({
        EmailAlreadyExistsException.class,
        
        InvalidTokenException.class
    })
    
    public ResponseEntity<ErrorResponse> handleBadRequestExceptions(RuntimeException ex) {
        return new ResponseEntity<>(
            new ErrorResponse("BAD_REQUEST", ex.getMessage()),
            HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler({
        UserNotFoundException.class,
        ResourceNotFoundException.class
    })
    public ResponseEntity<ErrorResponse> handleNotFoundExceptions(RuntimeException ex) {
        return new ResponseEntity<>(
            new ErrorResponse("NOT_FOUND", ex.getMessage()),
            HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler({
        TokenExpiredException.class,
        ExpiredJwtException.class
    })
    public ResponseEntity<ErrorResponse> handleTokenExpiredExceptions(RuntimeException ex) {
        return new ResponseEntity<>(
            new ErrorResponse("TOKEN_EXPIRED", ex.getMessage()),
            HttpStatus.UNAUTHORIZED
        );
    }

    @ExceptionHandler(AccountDisabledException.class)
    public ResponseEntity<ErrorResponse> handleAccountSisabledExceptions(AccountDisabledException ex) {
        return new ResponseEntity<>(
            new ErrorResponse("ACCOUNT_DISABLED", "Account not verified"),
            HttpStatus.FORBIDDEN
        );
    }

   
    
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        ErrorResponse error = new ErrorResponse("INVALID_CREDENTIALS", "Invalid email or password");
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }
    
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorResponse> handledNonEnabledAccountException(DisabledException ex){
    	ErrorResponse err = new ErrorResponse("ACCOUNT_DISABLED", "User account is disabled.");
    	return new ResponseEntity<>(err,HttpStatus.FORBIDDEN);
    }
    
    
    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ErrorResponse>handleLockedException(LockedException ex){
    	ErrorResponse error = new ErrorResponse("ACCOUNT_LOCKED", "User account is blocked.");
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied() {
        return new ResponseEntity<>(
            new ErrorResponse("ACCESS_DENIED", "You don't have permission to access this resource"),
            HttpStatus.FORBIDDEN
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation() {
        return new ResponseEntity<>(
            new ErrorResponse("DATA_CONFLICT", "Data integrity violation occurred"),
            HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex) {
    	ErrorResponse error = new ErrorResponse("Internal Server Error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(error);
        
    }
    
    @ExceptionHandler(PasswordMismatchException.class)
    public ResponseEntity<ErrorResponse> handlePasswordMismatch(PasswordMismatchException ex) {
        return new ResponseEntity<>(
            new ErrorResponse("PASSWORD_MISMATCH", ex.getMessage()),
            HttpStatus.BAD_REQUEST
        );
    }

    
    

}