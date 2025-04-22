package com.sunbeam.service;

import com.sunbeam.dto.response.DocumentApplicationResponse;
import com.sunbeam.dto.response.VerificationStatsResponse;
import com.sunbeam.model.DocumentApplication;
import org.springframework.data.domain.Page;

public interface VerificationService {
    DocumentApplicationResponse assignToVerifier(Long applicationId, Long verifierId);
    DocumentApplicationResponse approveApplication(Long applicationId, String remarks);
    DocumentApplicationResponse rejectApplication(Long applicationId, String remarks);
    DocumentApplicationResponse requestChanges(Long applicationId, String remarks);
   // Page<DocumentApplicationResponse> getAssignedApplications(int page, int size);
 //   VerificationStatsResponse getVerificationStats();
//    DocumentApplicationResponse escalateToSeniorVerifier(Long applicationId, String reason);
    
}