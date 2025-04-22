package com.sunbeam.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class EmailRequest {
    @Email(message = "Invalid email format")
    @NotBlank(message = "Recipient email is required")
    private String toEmail;
    
    @NotBlank(message = "Subject is required")
    private String subject;
    
    @NotBlank(message = "Body is required")
    private String body;
}