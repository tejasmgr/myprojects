package com.sunbeam.controller;
import com.sunbeam.dto.response.DocumentApplicationDetailsResponse;
import com.sunbeam.dto.response.DocumentApplicationResponse;
import com.sunbeam.dto.response.VerificationStatsResponse;
import com.sunbeam.model.DocumentApplication;
import com.sunbeam.service.DocumentService;
import com.sunbeam.service.VerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/verifier")
@RequiredArgsConstructor

public class VerifierController {

    private final VerificationService verificationService;
    private final DocumentService documentService;
    
    @GetMapping("/application/{id}")
    public ResponseEntity<DocumentApplicationResponse> getApplicationById(@PathVariable long id){
    	 DocumentApplicationResponse application = documentService.getApplicationById(id);
    	    return ResponseEntity.ok(application);
    	    
    }
    
    @GetMapping("/application/details/{applicationId}")
    @PreAuthorize("hasRole('VERIFIER') or hasRole('ADMIN')")
    public ResponseEntity<DocumentApplicationDetailsResponse> getDocumentApplicationDetails(
            @PathVariable Long applicationId) {
        DocumentApplicationDetailsResponse applicationDetails = documentService.getDocumentApplicationDetails(applicationId);
        if (applicationDetails != null) {
            return ResponseEntity.ok(applicationDetails);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    

    @GetMapping("/pending")
    public ResponseEntity<Page<DocumentApplicationResponse>> getPendingApplications(@PageableDefault(size = 20) Pageable pageable) {
    	System.out.println("Insoide Verification COntrollerr");
        return ResponseEntity.ok(verificationService.getPendingApplications(pageable));
    }
    
    @PostMapping("/{applicationId}/approve")
    @PreAuthorize("hasRole('VERIFIER') or hasRole('ADMIN')")
    public ResponseEntity<DocumentApplicationResponse> approveApplication(
            @PathVariable Long applicationId,
            @RequestParam String remarks) {
        return ResponseEntity.ok(
            verificationService.approveApplication(applicationId, remarks)
        );
    }
    
    @PostMapping("/{applicationId}/reject")
    public ResponseEntity<DocumentApplicationResponse> rejectApplication(
    		@PathVariable Long applicationId,
            @RequestParam String remarks) {
        return ResponseEntity.ok(verificationService.rejectApplication(applicationId, remarks));
    }

    @PostMapping("/{applicationId}/request-change")
    public ResponseEntity<DocumentApplicationResponse> moveToNextDesk(
    		@PathVariable Long applicationId,
            @RequestParam String remarks) {
        return ResponseEntity.ok(verificationService.requestChanges(applicationId, remarks));
    }
    
    @GetMapping("/stats")
    public ResponseEntity<VerificationStatsResponse> getVerificationStats() {
        return ResponseEntity.ok(verificationService.getVerificationStats());
    }
}