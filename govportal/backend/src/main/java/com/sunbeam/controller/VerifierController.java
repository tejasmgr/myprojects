package com.sunbeam.controller;
import com.sunbeam.dto.response.DocumentApplicationDetailsResponse;
import com.sunbeam.dto.response.DocumentApplicationResponse;
import com.sunbeam.dto.response.VerificationStatsResponse;
import com.sunbeam.model.DocumentApplication;
import com.sunbeam.model.DocumentProof;
import com.sunbeam.service.DocumentService;
import com.sunbeam.service.VerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.apache.tika.metadata.HttpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/verifier")
@RequiredArgsConstructor

public class VerifierController {

    private final VerificationService verificationService;
    private final DocumentService documentService;
    
    @Value("${app.upload.dir}") // From application.properties
	private String fileStorageLocation;
    
    
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
    
	@GetMapping
	public ResponseEntity<Resource> getDocumentProof(@PathVariable Long proofId) {
		DocumentProof proof = documentService.getDocumentProof(proofId);
		if (proofId == null) {
			return ResponseEntity.notFound().build();
		}
		Path file = Paths.get(fileStorageLocation).resolve(proof.getFilePath()).normalize();
		Resource resource;
		try {
			resource = new UrlResource(file.toUri());
		} catch (MalformedURLException e) {
			return ResponseEntity.notFound().build();
		}
		if (resource.exists()) {
			return ResponseEntity.ok().contentType(MediaType.parseMediaType(proof.getContentType()))
					.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\""+proof.getFileName()+ "\"")
					.body(resource);
		}else {
			return ResponseEntity.notFound().build();		}
	}

    @GetMapping("/pending")
    public ResponseEntity<Page<DocumentApplicationResponse>> getPendingApplications(@PageableDefault(size = 20) Pageable pageable) {
    	
        return ResponseEntity.ok(verificationService.getPendingApplications(pageable));
    }
    
    @PostMapping("/approve/{applicationId}")
    @PreAuthorize("hasRole('VERIFIER') or hasRole('ADMIN')")
    public ResponseEntity<DocumentApplicationResponse> approveApplication(
            @PathVariable Long applicationId,
            @RequestParam String remarks) {
        return ResponseEntity.ok(
            verificationService.approveApplication(applicationId, remarks)
        );
    }
    
    @GetMapping("applications/approved/{verifierId}")
    @PreAuthorize("hasRole('VERIFIER') or hasRole('ADMIN')")
    public ResponseEntity<Page<DocumentApplicationResponse>> getApprovedApplicationsByVerifier(
    		@PageableDefault(size = 20) Pageable pageable, 
    		@PathVariable long verifierId){
		return verificationService.getApprovedApplicationsByVerifier(pageable, verifierId);	
    }
    
    @PostMapping("/reject/{applicationId}")
    @PreAuthorize("hasRole('VERIFIER') or hasRole('ADMIN')")
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