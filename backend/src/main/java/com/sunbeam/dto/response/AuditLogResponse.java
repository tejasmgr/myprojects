package com.sunbeam.dto.response;

import com.sunbeam.model.AuditLog.ActionType;
import com.sunbeam.dto.response.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {
    private Long id;
    private UserResponse user;
    private String action;
    private String details;
    private LocalDateTime timestamp;
    private ActionType actionType;
}