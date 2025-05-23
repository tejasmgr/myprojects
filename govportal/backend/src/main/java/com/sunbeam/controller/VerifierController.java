package com.sunbeam.controller;
import com.sunbeam.dto.response.DocumentApplicationDetailsResponse;
import com.sunbeam.dto.response.DocumentApplicationResponse;
import com.sunbeam.dto.response.VerificationStatsResponse;
import com.sunbeam.exception.ResourceNotFoundException;
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

import java.io.IOException;
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
    
    
    
    /**
     * Endpoint to view or download a specific document proof, accessible by Verifiers.
     * This endpoint retrieves the document proof as a Resource from the service layer
     * and serves it via HTTP with appropriate headers for inline viewing.
     *
     * @param documentProofId The ID of the document proof to retrieve.
     * @return ResponseEntity containing the file as a Resource, with appropriate headers.
     * @throws IOException If there's an issue loading the file from the service.
     * @throws ResourceNotFoundException If the document proof is not found.
     */
    @GetMapping("/proofs/{documentProofId}/view") // NEW ENDPOINT PATH
    @PreAuthorize("hasRole('VERIFIER') or hasRole('ADMIN')") // Ensure only VERIFIERs can access this
    public ResponseEntity<Resource> viewDocumentProof(@PathVariable Long documentProofId) throws IOException {
        try {
            Resource resource = verificationService.viewDocumentProof(documentProofId);
            String contentType = verificationService.getDocumentProofContentType(documentProofId);

            if (contentType == null || contentType.isBlank()) {
                contentType = "application/octet-stream"; // Fallback
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (ResourceNotFoundException e) {
            // Return 404 Not Found for specific resource not found exceptions
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            // Handle file loading errors
            return ResponseEntity.status(500).body(null); // Or a more specific error body
        }
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