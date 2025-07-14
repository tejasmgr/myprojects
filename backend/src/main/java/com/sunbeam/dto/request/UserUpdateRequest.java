package com.sunbeam.dto.request;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserUpdateRequest {
    @NotBlank(message = "First name is required")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    private String lastName;
    
    @NotBlank(message = "Address is required")
    private String address;
    
    private LocalDate dateOfBirth;
    
    private String gender;
    
    private String fatherName;
    
    @Column(unique = true)
    private String aadharNumber;

}