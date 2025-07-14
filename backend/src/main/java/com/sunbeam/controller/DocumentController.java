package com.sunbeam.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sunbeam.dto.request.DocumentApplicationRequest;
import com.sunbeam.dto.response.DocumentApplicationDetailsResponse;
import com.sunbeam.dto.response.DocumentApplicationResponse;
import com.sunbeam.exception.DatabaseOperationException;
import com.sunbeam.exception.FileStorageException;
import com.sunbeam.exception.InvalidDocumentTypeException;
import com.sunbeam.exception.ResourceNotFoundException;
import com.sunbeam.model.CustomUserDetails;
import com.sunbeam.model.DocumentApplication;
import com.sunbeam.model.DocumentApplication.DocumentType;
import com.sunbeam.model.User;
import com.sunbeam.service.DocumentService;
import com.sunbeam.service.VerificationWorkflowService;
import com.sunbeam.service.impl.DocumentServiceImpl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import org.apache.tika.metadata.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

	private final DocumentServiceImpl documentService;
	
//	private final VerificationWorkflowService workflowService;
	@Autowired
	private ObjectMapper objectMapper;
	Logger logger = LoggerFactory.getLogger(DocumentController.class);
	
	@PostMapping("/submit")
	public ResponseEntity<String> submitDocumentApplication(
			@Valid @RequestPart("applicationData") String applicationRequest,
			@RequestPart("documents") List<MultipartFile> documents,
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		try {
			DocumentApplicationRequest documentApplicationRequest = objectMapper.readValue(applicationRequest, DocumentApplicationRequest.class);
			// 1. Get the authenticated user
			User applicant = userDetails.getUser(); 
			// 2. Call the service to submit the application
			DocumentApplication application =  documentService.submitApplication(applicant, documentApplicationRequest, documents);
			return ResponseEntity.status(HttpStatus.CREATED).body("Application submitted successfully with Application ID : " + application.getId() );
		}
		catch (jakarta.validation.ConstraintViolationException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Validation error: " + e.getMessage());
		} catch (InvalidDocumentTypeException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		} catch (IOException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Failed to upload documents: " + e.getMessage());
		} catch (FileStorageException e) { // Catch your custom exception
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("File storage error: " + e.getMessage());
		} catch (DatabaseOperationException e) { // Catch your custom exception
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Database error: " + e.getMessage());
		} catch (Exception e) {
		    logger.error("Unexpected error in submitDocumentApplication", e);
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + e.getMessage());
		}
	}

//	@GetMapping("/my-applications")
//	public ResponseEntity<List<DocumentApplicationResponse>> getUserApplications() {
//		return ResponseEntity.ok(documentService.getUserApplications());
//	}
	
	
	@GetMapping("/applications")
	public ResponseEntity<Page<DocumentApplicationResponse>> getAllApplications(@PageableDefault(size = 20) Pageable pageable) {
		try {
			return ResponseEntity.ok(documentService.getAllApplications(pageable));
		} catch (DatabaseOperationException e) { // Catch your custom exception
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}catch (Exception e) {
			logger.error("Unexpected error while fetching applications", e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}
	
	@GetMapping("/citizen/approvalPassed-applications")
	public ResponseEntity<Page<DocumentApplicationResponse>> getApprovalPassedApplications(@PageableDefault(size = 20) Pageable pageable) {
		try {
			return ResponseEntity.ok(documentService.getApprovalPassesApplicationsOfCitizen(pageable));
		} catch (DatabaseOperationException e) { // Catch your custom exception
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}catch (Exception e) {
			logger.error("Unexpected error while fetching applications", e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}
	

	@GetMapping("application/{id}")
	public ResponseEntity<DocumentApplicationResponse> getApplication(@PathVariable Long id) {
		try {
	        return ResponseEntity.ok(documentService.getApplicationById(id));
	    } catch (ResourceNotFoundException e) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
	    } catch (Exception e) {
	        logger.error("Unexpected error while fetching application with ID: {}", id, e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
	    }
	}
	
	@GetMapping("application-details/{id}")
	public ResponseEntity<DocumentApplicationDetailsResponse> getApplicationDetails(@PathVariable Long id) {
		try {
	        return ResponseEntity.ok(documentService.getDocumentApplicationDetails(id));
	    } catch (ResourceNotFoundException e) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
	    } catch (Exception e) {
	        logger.error("Unexpected error while fetching application with ID: {}", id, e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
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
    
    public ResponseEntity<Resource> viewDocumentProof(@PathVariable Long documentProofId) throws IOException {
        try {
            Resource resource = documentService.viewDocumentProof(documentProofId);
            String contentType = documentService.getDocumentProofContentType(documentProofId);

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
	
	

	@GetMapping("certificate/{id}/download")
	public ResponseEntity<byte[]> downloadCertificate(@PathVariable Long id) {
		try {
	        byte[] pdfBytes = documentService.getCertificatePdf(id);
	        return ResponseEntity.ok()
	                .header("Content-Type", "application/pdf")
	                .header("Content-Disposition", "attachment; filename=\"certificate.pdf\"")
	                .body(pdfBytes);
	    } catch (ResourceNotFoundException e) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
	    } catch (IllegalStateException e) {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
	    } catch (Exception e) {
	        logger.error("Unexpected error while downloading certificate for application ID: {}", id, e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
	    }
	}
}