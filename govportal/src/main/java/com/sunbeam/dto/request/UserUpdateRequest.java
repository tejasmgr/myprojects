package com.sunbeam.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserUpdateRequest {
    @NotBlank(message = "First name is required")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    private String lastName;
    
    @NotBlank(message = "Address is required")
    private String address;
}