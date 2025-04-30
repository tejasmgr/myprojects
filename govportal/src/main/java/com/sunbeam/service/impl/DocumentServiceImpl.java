package com.sunbeam.service.impl;

import com.sunbeam.dto.request.DocumentApplicationRequest;
import com.sunbeam.dto.response.DocumentApplicationResponse;
import com.sunbeam.exception.*;
import com.sunbeam.model.*;
import com.sunbeam.repository.DocumentApplicationRepository;
import com.sunbeam.repository.DocumentProofRepository;
import com.sunbeam.repository.UserRepository;
import com.sunbeam.security.SecurityUtils;
import com.sunbeam.service.AuditService;
import com.sunbeam.service.DocumentService;
import com.sunbeam.service.FileStorageService;
import com.sunbeam.service.PdfGeneratorService;
import com.sunbeam.service.VerificationWorkflowService;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

	private final DocumentApplicationRepository documentRepository;
	private final UserRepository userRepository;
	private final FileStorageService fileStorageService;
	private final PdfGeneratorService pdfGenerator;
	private final VerificationWorkflowService workflowService;
	private final AuditService auditService;
	private final SecurityUtils securityUtils;
	private final ModelMapper modelMapper;
	private final DocumentProofRepository documentProofRepository;

	@Value("${app.upload.dir}") // From application.properties
	private String uploadDir;
	private static final Logger logger = LoggerFactory.getLogger(DocumentServiceImpl.class);


	@Transactional(rollbackFor = Exception.class)
	public DocumentApplication submitApplication(User applicant, DocumentApplicationRequest request, List<MultipartFile> files) throws IOException {
	    logger.info("Starting document application submission for user: {}", applicant.getEmail());

	    // Step 1 - Create DocumentApplication entity
	    logger.info("Creating DocumentApplication entity from request");
	    DocumentApplication application = DocumentApplication.builder()
	            .applicant(applicant)
	            .documentType(DocumentApplication.DocumentType.fromString(request.getDocumentType()))
	            .purpose(request.getPurpose())
	            .formData(request.getFormData())
	            .status(DocumentApplication.ApplicationStatus.PENDING)
	            .submissionDate(LocalDateTime.now())
	            .build();

	    logger.debug("DocumentApplication entity created: {}", application);

	    // Step 2 - Assign to first verifier
	    logger.info("Fetching first verifier from database");
	    User firstVerifier = userRepository.findByEmail("verifier1@example.com")
	            .orElseThrow(() -> new RuntimeException("First verifier not found"));
	    application.setAssignedVerifiers(List.of(firstVerifier));
	    application.setCurrentDesk("DESK_1");

	    // Step 3 - Save the Application
	    logger.info("Saving initial DocumentApplication to database");
	    application = documentRepository.save(application);
	    logger.info("DocumentApplication saved with ID: {}", application.getId());

	    // Step 4 - Process and save uploaded files
	    List<DocumentProof> documentProofs = new ArrayList<>();
	    logger.info("Processing uploaded files, total files received: {}", files.size());

	    try {
	        for (MultipartFile file : files) {
	            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
	            Path filePath = Paths.get(uploadDir, fileName).toAbsolutePath().normalize();
	            Files.copy(file.getInputStream(), filePath);

	            DocumentProof documentProof = DocumentProof.builder()
	                    .application(application)
	                    .fileName(file.getOriginalFilename())
	                    .filePath(filePath.toString())
	                    .contentType(file.getContentType())
	                    .build();
	            documentProofRepository.save(documentProof);
	            documentProofs.add(documentProof);

	            logger.info("Stored document: {} at path: {}", file.getOriginalFilename(), filePath.toString());
	        }

	        application.setDocumentProofs(documentProofs); // Associate documents
	        logger.debug("Associated {} document proofs with the application", documentProofs.size());

	        logger.info("Saving updated DocumentApplication with document proofs");
	        documentRepository.save(application);

	    } catch (IOException e) {
	        logger.error("IOException occurred while storing files: {}", e.getMessage(), e);
	        throw new FileStorageException("Failed to store document: " + e.getMessage(), e);
	    } catch (org.springframework.dao.DataAccessException e) {
	        logger.error("Database exception while saving document proofs: {}", e.getMessage(), e);
	        throw new DatabaseOperationException("Error saving document details to the database", e);
	    }

	    logger.info("Document application submitted successfully for user: {}", applicant.getEmail());
	    
	    return application;
	}

	@Override
	@Transactional(readOnly = true)
	public DocumentApplicationResponse getApplicationById(Long id) {
		DocumentApplication application = documentRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Application not found"));

		verifyAccessPermissions(application);
		return modelMapper.map(application, DocumentApplicationResponse.class);
	}

	@Override
	@Transactional(readOnly = true)
	public List<DocumentApplicationResponse> getUserApplications() {
		User currentUser = securityUtils.getCurrentUser();
		return documentRepository.findByApplicant(currentUser).stream()
				.map(app -> modelMapper.map(app, DocumentApplicationResponse.class)).collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	public Page<DocumentApplicationResponse> getAllApplications(Pageable pageable) {
		return documentRepository.findAll(pageable).map(app -> modelMapper.map(app, DocumentApplicationResponse.class));
	}

	@Override
	@Transactional
	public byte[] generateCertificatePdf(Long applicationId) {
		DocumentApplication application = documentRepository.findById(applicationId)
				.orElseThrow(() -> new ResourceNotFoundException("Application not found"));

		if (application.getStatus() != DocumentApplication.ApplicationStatus.APPROVED) {
			throw new IllegalStateException("Only approved applications can generate certificates");
		}

		try {
			return pdfGenerator.generateCertificate(application);
		} catch (IOException e) {
			throw new PdfGenerationException("Failed to generate PDF certificate");
		}
	}

	@Override
	@Transactional
	public DocumentApplicationResponse approveApplication(Long applicationId, String remarks) {
		DocumentApplication application = documentRepository.findById(applicationId)
				.orElseThrow(() -> new ResourceNotFoundException("Application not found"));

		User currentUser = securityUtils.getCurrentUser();
		if (!application.getAssignedVerifiers().contains(currentUser)) {
			throw new UnauthorizedAccessException("You are not assigned to verify this application");
		}

		application.setStatus(DocumentApplication.ApplicationStatus.APPROVED);
		application.setResolvedDate(LocalDateTime.now());
		application.setApprovedBy(currentUser);
		DocumentApplication savedApp = documentRepository.save(application);

		auditService.logDocumentStatusChange(applicationId, "DOCUMENT_APPROVED",
				"Approved by " + currentUser.getEmail() + " with remarks: " + remarks);

		return modelMapper.map(savedApp, DocumentApplicationResponse.class);
	}

	@Override
	@Transactional
	public DocumentApplicationResponse rejectApplication(Long applicationId, String remarks) {
		DocumentApplication application = documentRepository.findById(applicationId)
				.orElseThrow(() -> new ResourceNotFoundException("Application not found"));

		application.setStatus(DocumentApplication.ApplicationStatus.REJECTED);
		application.setRejectionReason(remarks);
		application.setResolvedDate(LocalDateTime.now());
		DocumentApplication savedApp = documentRepository.save(application);

		auditService.logDocumentStatusChange(applicationId, "DOCUMENT_REJECTED", "Rejected with remarks: " + remarks);

		return modelMapper.map(savedApp, DocumentApplicationResponse.class);
	}

	@Override
	@Transactional
	public String uploadSupportingDocument(Long applicationId, MultipartFile file) {
//		DocumentApplication application = documentRepository.findById(applicationId)
//				.orElseThrow(() -> new ResourceNotFoundException("Application not found"));
//
//		if (application.getStatus() != DocumentApplication.ApplicationStatus.PENDING) {
//			throw new IllegalStateException("Cannot add documents to processed applications");
//		}
//
//		try {
//			String filePath = fileStorageService.storeFile(file);
//			application.getDocumentPaths().add(filePath);
//			documentRepository.save(application);
//
//			auditService.logActivity("DOCUMENT_UPLOAD", "Added supporting document to application " + applicationId);
//
//			return filePath;
//		} catch (IOException e) {
//			throw new FileStorageException("Failed to store document: " + e.getMessage());
//		}

		return null;
	}

	private void verifyAccessPermissions(DocumentApplication application) {
		User currentUser = securityUtils.getCurrentUser();

		if (!application.getApplicant().equals(currentUser) && !application.getAssignedVerifiers().contains(currentUser)
				&& !currentUser.getRole().equals(User.Role.ADMIN)) {
			throw new UnauthorizedAccessException("You don't have permission to access this application");
		}
	}

	@Override
	public DocumentApplicationResponse reassignApplication(Long applicationId, Long newVerifierId) {
		// TODO Auto-generated method stub
		return null;
	}

}
