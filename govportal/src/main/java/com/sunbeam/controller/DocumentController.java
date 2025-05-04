package com.sunbeam.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sunbeam.dto.request.DocumentApplicationRequest;
import com.sunbeam.dto.response.DocumentApplicationResponse;
import com.sunbeam.model.CustomUserDetails;
import com.sunbeam.model.DocumentApplication.DocumentType;
import com.sunbeam.model.User;
import com.sunbeam.service.DocumentService;
import com.sunbeam.service.VerificationWorkflowService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;



@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

	private final DocumentService documentService;
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
			documentService.submitApplication(applicant, documentApplicationRequest, documents);
			return ResponseEntity.status(HttpStatus.CREATED).body("Application submitted successfully");
		}
		catch (jakarta.validation.ConstraintViolationException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Validation error: " + e.getMessage());
		} catch (com.sunbeam.exception.InvalidDocumentTypeException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		} catch (IOException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Failed to upload documents: " + e.getMessage());
		} catch (com.sunbeam.exception.FileStorageException e) { // Catch your custom exception
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("File storage error: " + e.getMessage());
		} catch (com.sunbeam.exception.DatabaseOperationException e) { // Catch your custom exception
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
		return ResponseEntity.ok(documentService.getAllApplications(pageable));
	}
	
	
	

	@GetMapping("/{id}")
	public ResponseEntity<DocumentApplicationResponse> getApplication(@PathVariable Long id) {
		return ResponseEntity.ok(documentService.getApplicationById(id));
	}

	@GetMapping("/{id}/download")
	public ResponseEntity<byte[]> downloadCertificate(@PathVariable Long id) {
		byte[] pdfBytes = documentService.generateCertificatePdf(id);
		return ResponseEntity.ok().header("Content-Type", "application/pdf")
				.header("Content-Disposition", "attachment; filename=\"certificate.pdf\"").body(pdfBytes);
	}
}