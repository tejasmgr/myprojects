package com.sunbeam.service.impl;

import com.sunbeam.dto.response.AuditLogResponse;
import com.sunbeam.model.AuditLog;
import com.sunbeam.model.User;
import com.sunbeam.repository.AuditLogRepository;
import com.sunbeam.repository.DocumentApplicationRepository;
import com.sunbeam.repository.UserRepository;
import com.sunbeam.security.SecurityUtils;
import com.sunbeam.service.AuditService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

	private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final DocumentApplicationRepository documentRepository;
    private final SecurityUtils securityUtils;
    private final ModelMapper modelMapper;

    

  

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getAuditLogs(org.springframework.data.domain.Pageable pageable) {
        return auditLogRepository.findAll(pageable)
                .map(log -> modelMapper.map(log, AuditLogResponse.class));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getUserAuditLogs(Long userId, org.springframework.data.domain.Pageable pageable) {
        return auditLogRepository.findByUserId(userId, pageable)
                .map(log -> modelMapper.map(log, AuditLogResponse.class));
    }

    @Override
    @Transactional
    public void logActivity(String action, String details) {
        User currentUser = securityUtils.getCurrentUser();
        
        AuditLog log = AuditLog.builder()
                .user(currentUser)
                .action(action)
                .details(details)
                .timestamp(LocalDateTime.now())
                .build();
        
        auditLogRepository.save(log);
    }

    @Override
    @Transactional
    public void logDocumentStatusChange(Long documentId, String action, String remarks) {
        User currentUser = securityUtils.getCurrentUser();
        
        AuditLog log = AuditLog.builder()
                .user(currentUser)
                .action("DOCUMENT_" + action)
                .details(String.format(
                    "Document ID: %d | Action: %s | Remarks: %s", 
                    documentId, action, remarks
                ))
                .timestamp(LocalDateTime.now())
                .build();
        
        auditLogRepository.save(log);
    }
}