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
	public DocumentApplication submitApplication(User applicant, DocumentApplicationRequest request, List<MultipartFile> files) throws IOException {
   
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

		return modelMapper.map(application, DocumentApplicationResponse.class);
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

//	@Override
//	@Transactional(readOnly = true)
//	public List<DocumentApplicationResponse> getUserApplications() {
//		User currentUser = securityUtils.getCurrentUser();
//		List<DocumentApplication> userApplications = documentRepository.findByApplicant(currentUser);
//		
//		return documentRepository.findByApplicant(currentUser).stream()
//				.map(app -> modelMapper.map(app, DocumentApplicationResponse.class)).collect(Collectors.toList());
//	}

	@Override
	@Transactional(readOnly = true)
	public Page<DocumentApplicationResponse> getAllApplications(Pageable pageable) {
		return documentRepository.findAll(pageable).map(app -> modelMapper.map(app, DocumentApplicationResponse.class));
	}

	@Override
	@Transactional
	public byte[] getCertificatePdf(Long applicationId) {
		DocumentApplication application = documentRepository.findById(applicationId)
				.orElseThrow(() -> new ResourceNotFoundException("Application not found"));

		if (application.getStatus() != DocumentApplication.ApplicationStatus.APPROVED) {
			throw new IllegalStateException("Only approved applications can generate certificates");
		}
		return application.getCertificatePdf();

//		try {
//			return pdfGenerator.generateCertificate(application);
//		} catch (IOException e) {
//			throw new PdfGenerationException("Failed to generate PDF certificate");
//		}
	}







	@Override
	public DocumentProof getDocumentProof(Long proofId) {
		
		return documentProofRepository.getById(proofId);
	}



}
