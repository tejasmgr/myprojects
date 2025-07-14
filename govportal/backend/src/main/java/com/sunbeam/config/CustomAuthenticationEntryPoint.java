package com.sunbeam.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sunbeam.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        ErrorResponse error = new ErrorResponse("INVALID_CREDENTIALS", "Invalid username or password");

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        new ObjectMapper().writeValue(response.getOutputStream(), error);
    }
}
