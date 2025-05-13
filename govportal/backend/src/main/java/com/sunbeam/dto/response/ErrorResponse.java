package com.sunbeam.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private String code;
    private String message;
    private Map<String, String> errors;
    
    public ErrorResponse(String code, String message) {
        this.code = code;
        this.message = message;
    }
}