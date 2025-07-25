package com.sunbeam.dto.request;

import java.time.LocalDate;

import com.sunbeam.model.User.Designation;

import jakarta.persistence.Column;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateVerifierRequest {
    @NotBlank(message = "Full name is required")
    private String fullName;
    
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;
    
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
             message = "Password must be 8+ chars with letters, numbers, and special chars")
    private String password;
    
    @NotBlank(message = "Designation is required")
    
    private String designation;
    
    private String address;
    
    private LocalDate dateOfBirth;
    
    private String gender;
    
    @Column(unique = true)
    private String aadharNumber;
}