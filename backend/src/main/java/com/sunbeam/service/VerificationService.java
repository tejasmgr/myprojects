package com.sunbeam.service;

import com.sunbeam.dto.response.DocumentApplicationDetailsResponse;
import com.sunbeam.dto.response.DocumentApplicationResponse;
import com.sunbeam.dto.response.VerificationStatsResponse;
import com.sunbeam.model.DocumentApplication;

import java.io.IOException;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

public interface VerificationService {
	
	ResponseEntity<DocumentApplicationResponse> getDocumentApplicationById(Long id);
    DocumentApplicationResponse approveApplication(Long applicationId, String remarks);
    DocumentApplicationResponse rejectApplication(Long applicationId, String remarks);
    DocumentApplicationResponse requestChanges(Long applicationId, String remarks);
//    Page<DocumentApplicationResponse> getAssignedApplications(int page, int size);
    VerificationStatsResponse getVerificationStats();
//    DocumentApplicationResponse escalateToSeniorVerifier(Long applicationId, String reason);
    Page<DocumentApplicationResponse>  getPendingApplications(Pageable pageable);
	ResponseEntity<Page<DocumentApplicationResponse>> getApprovedApplicationsByVerifier(Pageable pageable, long verifierId);
	Resource viewDocumentProof(Long documentProofId) throws IOException;
	String getDocumentProofContentType(Long documentProofId);
	
	
    
}