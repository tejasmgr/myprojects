package com.sunbeam.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ApplicationActionRequest {
    @NotNull(message = "Application ID is required")
    private Long applicationId;
    
    @NotBlank(message = "Remarks are required")
    private String remarks;

}