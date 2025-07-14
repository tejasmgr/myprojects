package com.sunbeam.model;



import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    private User user;
    
    private String action;
    private String details;
    
    private LocalDateTime timestamp;
    
    @Enumerated(EnumType.STRING)
    private ActionType actionType;

    public enum ActionType {
        LOGIN, DOCUMENT_SUBMIT, DOCUMENT_APPROVE, DOCUMENT_REJECT
    }
}
