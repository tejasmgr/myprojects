package com.sunbeam.dto.request;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "First name is required")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    private String lastName;
    
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;
    
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
             message = "Password must be 8+ chars with letters, numbers, and special chars")
    private String password;
    
    @NotBlank(message = "Address is required")
    private String address;
    
    @NotBlank(message = "Aadhar number is required")
    @Size(min = 12, max = 12, message = "Aadhar must be 12 digits")
    private String aadharNumber;
}