package com.sunbeam.exception;

import com.sunbeam.dto.response.ErrorResponse;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
        PasswordMismatchException.class,
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

    @ExceptionHandler({
        AccountDisabledException.class,
        AccountBlockedException.class
    })
    public ResponseEntity<ErrorResponse> handleAccountExceptions(RuntimeException ex) {
        return new ResponseEntity<>(
            new ErrorResponse("ACCOUNT_ISSUE", ex.getMessage()),
            HttpStatus.FORBIDDEN
        );
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials() {
        return new ResponseEntity<>(
            new ErrorResponse("AUTH_ERROR", "Invalid username or password"),
            HttpStatus.UNAUTHORIZED
        );
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
        return new ResponseEntity<>(
            new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred"),
            HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

}