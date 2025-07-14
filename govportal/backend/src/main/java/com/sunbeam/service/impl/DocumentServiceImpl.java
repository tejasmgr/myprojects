package com.sunbeam.service.impl;

import com.sunbeam.dto.request.DocumentApplicationRequest;
import com.sunbeam.dto.response.DocumentApplicationDetailsResponse;
import com.sunbeam.dto.response.DocumentApplicationResponse;
import com.sunbeam.dto.response.DocumentProofResponse;
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
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
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
//	private final VerificationWorkflowService workflowService;
	private final AuditService auditService;
	private final SecurityUtils securityUtils;
	private final ModelMapper modelMapper;
	private final DocumentProofRepository documentProofRepository;

	@Value("${app.upload.dir}") // From application.properties
	private String uploadDir;
	private static final Logger logger = LoggerFactory.getLogger(DocumentServiceImpl.class);

	@Override
	@Transactional(rollbackFor = Exception.class)
	public DocumentApplication submitApplication(User applicant, DocumentApplicationRequest request, List<MultipartFile> files)  {
		
	    DocumentApplication application = DocumentApplication.builder()
	            .applicant(applicant)
	            .documentType(DocumentApplication.DocumentType.fromString(request.getDocumentType()))
	            .purpose(request.getPurpose())
	            .formData(request.getFormData())
	            .status(DocumentApplication.ApplicationStatus.PENDING)
	            .submissionDate(LocalDateTime.now())
	            .currentDesk("DESK_1")
	            .build();    
//	    application.setCurrentDesk("DESK_1"); 
	    application = documentRepository.save(application);
	    // Step 4 - Process and save uploaded files
	    List<DocumentProof> documentProofs = new ArrayList<>();
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
	        }
	        
	        application.setDocumentProofs(documentProofs); // Associate documents
	        documentRepository.save(application);
	        logger.info("Document application submitted successfully for user: {}", applicant.getEmail());
		    return application;
	        
	    } catch (IOException e) {
	        logger.error("IOException occurred while storing files: {}", e.getMessage(), e);
	        throw new FileStorageException("Failed to store document: " + e.getMessage(), e);
	    }catch (DatabaseOperationException e) {
			throw new DatabaseOperationException("DataBase Operation Failed : ");
		}catch (FileStorageException e) {
			throw new FileStorageException("Error storing uploaded files. Please try again.");
		}catch (InvalidDocumentTypeException e) {
			throw new InvalidDocumentTypeException("Invalid Document Type" );
		}catch (Exception e) {
			logger.error("Unexpected error submitting application: {}", e.getMessage(), e);
            throw new DatabaseOperationException("Failed to submit the application. Please try again.");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public Page<DocumentApplicationResponse> getAllApplications(Pageable pageable) {
		try {
			User currentUser =securityUtils.getCurrentUser();
			Page<DocumentApplication> applicationsList = documentRepository.findByApplicant(currentUser,pageable);
			return applicationsList.map(app -> modelMapper.map(app, DocumentApplicationResponse.class));
		} catch (DatabaseOperationException e) {
			logger.error("Error fetching all applications: {}", e.getMessage(), e);
			throw new DatabaseOperationException("Unable to Fetch Applications : "+ e.getMessage());
		}
	}

	@Override
	@Transactional(readOnly = true)
	public DocumentApplicationResponse getApplicationById(Long id) {
		try {
			DocumentApplication application = documentRepository.findById(id)
					.orElseThrow(() -> new ResourceNotFoundException("Application not found"));
			return modelMapper.map(application, DocumentApplicationResponse.class);
		} catch (Exception e) {
			logger.error("Error fetching application by id {}: {}", id, e.getMessage(), e);
            throw new DatabaseOperationException("Failed to fetch application.");
		}
	}
	
	@Override
	@Transactional
	public byte[] getCertificatePdf(Long applicationId) {
		try {
			DocumentApplication application = documentRepository.findById(applicationId)
					.orElseThrow(() -> new ResourceNotFoundException("Application not found with ID : " + applicationId ));

			if (application.getStatus() != DocumentApplication.ApplicationStatus.APPROVED) {
				throw new IllegalStateException("Certificate can only be downloaded for approved applications.");
			}
			return application.getCertificatePdf();
		} catch (ResourceNotFoundException | IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error generating certificate PDF for application id {}: {}", applicationId, e.getMessage(), e);
            throw new DatabaseOperationException("Failed to generate certificate PDF.");
        }

	}
	
	
	@Override
	@Transactional(readOnly = true)
	public DocumentApplicationDetailsResponse getDocumentApplicationDetails(long id) {
	
			DocumentApplication application = documentRepository.findById(id)
					.orElseThrow(() -> new DocumentNotFoundEception("DocumentApplication not found in Database"));
			
			
			
			List<DocumentProofResponse> proofResponses = application.getDocumentProofs().stream()
					.map(proof -> DocumentProofResponse.builder()
							.id(proof.getId())
							.fileName(proof.getFileName())
							.contentType(proof.getContentType())
							.fileUrl(proof.getFilePath())
							.build())
					.collect(Collectors.toList());		
		return DocumentApplicationDetailsResponse.builder()
				.id(application.getId())
				.applicantName(application.getApplicant().getFullName())
				.adhaarNumber(application.getApplicant().getAadharNumber())
				.formData(application.getFormData())
				.documentType(application.getDocumentType())
				.status(application.getStatus())
				.purpose(application.getPurpose())
				.rejectionReason(application.getRejectionReason())
				.approvedByUserId(application.getApprovedBy()!=null ? application.getApprovedBy().getId() : null)
				.currentDesk(application.getCurrentDesk())
				.submissionDate(application.getSubmissionDate())
				.resolvedDate(application.getResolvedDate())
				.lastUpdatedDate(application.getLastUpdatedDate())
				.applicantId(application.getApplicant().getId())
				.documentProofs(proofResponses)
				.build();
				
	}

	@Override
	public DocumentProof getDocumentProof(Long proofId) {
		
		return documentProofRepository.getById(proofId);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<DocumentApplicationResponse> getApprovalPassesApplicationsOfCitizen(Pageable pageable) {
		try {
			User currentUser =securityUtils.getCurrentUser();
			Page<DocumentApplication> applicationsList = documentRepository.findByApplicantAndStatus(currentUser,DocumentApplication.ApplicationStatus.APPROVED,pageable);
			return applicationsList.map(app -> modelMapper.map(app, DocumentApplicationResponse.class));
		} catch (DatabaseOperationException e) {
			logger.error("Error fetching Approval- Passed applications: {}", e.getMessage(), e);
			throw new DatabaseOperationException("Unable to Fetch Approved-Applications : "+ e.getMessage());
		}
	}
	
	
	/**
     * Retrieves a document proof as a Spring Resource for serving.
     * This method handles finding the document proof by ID, loading the file
     * from the local file system, and performing necessary checks.
     *
     * @param documentProofId The ID of the document proof.
     * @return A Spring Resource representing the file.
     * @throws ResourceNotFoundException If the document proof or the file is not found.
     * @throws IOException If there's an error loading the file from the file system.
     */
	public Resource viewDocumentProof(Long documentProofId) throws IOException {
        // 1. Retrieve the DocumentProof entity from the database
        DocumentProof documentProof = documentProofRepository.findById(documentProofId)
                .orElseThrow(() -> new ResourceNotFoundException("DocumentProof not found with ID: " + documentProofId));

        // 2. Construct the Path to the actual file on the server's file system
        Path filePath = Paths.get(documentProof.getFilePath()); // documentProof.getFilePath() should return "D:\\CDAC\\..."

        Resource resource;
        try {
            resource = new UrlResource(filePath.toUri()); // Convert local file path to a URL resource
        } catch (MalformedURLException ex) {
            throw new RuntimeException("Error creating URL for file: " + documentProof.getFileName(), ex);
        }

        // 3. Check if the file exists and is readable
        if (!resource.exists() || !resource.isReadable()) {
            throw new ResourceNotFoundException("File not found or not readable: " + documentProof.getFileName());
        }

        return resource;
    }
	
	/**
     * Retrieves the content type of a document proof.
     *
     * @param documentProofId The ID of the document proof.
     * @return The content type string (e.g., "application/pdf").
     * @throws ResourceNotFoundException If the document proof is not found.
     */
    public String getDocumentProofContentType(Long documentProofId) {
        DocumentProof documentProof = documentProofRepository.findById(documentProofId)
                .orElseThrow(() -> new ResourceNotFoundException("DocumentProof not found with ID: " + documentProofId));
        return documentProof.getContentType();
    }
	



}
