package com.sunbeam.service;
import com.sunbeam.dto.response.AuditLogResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuditService {
    Page<AuditLogResponse> getAuditLogs(org.springframework.data.domain.Pageable pageable);
    Page<AuditLogResponse> getUserAuditLogs(Long userId, org.springframework.data.domain.Pageable pageable);
    void logActivity(String action, String details);
    void logDocumentStatusChange(Long documentId, String action, String remarks);
    
}