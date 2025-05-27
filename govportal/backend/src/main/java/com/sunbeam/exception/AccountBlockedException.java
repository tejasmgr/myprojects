package com.sunbeam.exception;

import org.springframework.security.core.AuthenticationException;

public class AccountBlockedException extends RuntimeException {
    public AccountBlockedException(String message) {
        super(message);
    }
}