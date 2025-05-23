package com.sunbeam.service.impl;

import com.sunbeam.dto.response.DocumentApplicationDetailsResponse;
import com.sunbeam.dto.response.DocumentApplicationResponse;
import com.sunbeam.dto.response.DocumentProofResponse;
import com.sunbeam.dto.response.VerificationStatsResponse;
import com.sunbeam.exception.*;
import com.sunbeam.model.*;
import com.sunbeam.model.DocumentApplication.ApplicationStatus;
import com.sunbeam.repository.DocumentApplicationRepository;
import com.sunbeam.repository.DocumentProofRepository;
import com.sunbeam.repository.UserRepository;
import com.sunbeam.security.SecurityUtils;
import com.sunbeam.service.AuditService;
import com.sunbeam.service.VerificationService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VerificationServiceImpl implements VerificationService {

	private final DocumentApplicationRepository documentRepository;
	private final UserRepository userRepository;
	private final SecurityUtils securityUtils;
	private final AuditService auditService;
	private final ModelMapper modelMapper;
	private final PdfGeneratorServiceImpl pdfService;
	@Autowired
    private DocumentProofRepository documentProofRepository;

	@Override
	public ResponseEntity<DocumentApplicationResponse> getDocumentApplicationById(Long id) {
		DocumentApplication application = documentRepository.findById(id).orElseThrow(() -> new RuntimeException("DocumentApplication not found with id: " + id));
	    DocumentApplicationResponse dto = modelMapper.map(application, DocumentApplicationResponse.class);
	    return ResponseEntity.ok(dto);
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
	
	
	
	@Override
	public Page<DocumentApplicationResponse> getPendingApplications(Pageable pageable) {
		User verifier = securityUtils.getCurrentUser();
		Page<DocumentApplication> applicationsList = null;
		System.out.println("Inside Sevice of pending APplicationswith user name "+verifier.getFullName());
		if (verifier.getDesignation() == User.Designation.JUNIOR_VERIFIER) {
			System.out.println("in branch of junior verifier ");
			applicationsList = documentRepository.findByCurrentDesk("DESK_1", pageable);
		} else if (verifier.getDesignation() == User.Designation.SENIOR_VERIFIER) {
			applicationsList = documentRepository.findByCurrentDesk("DESK_2", pageable);
			System.out.println("in branch of senior verifier verifier ");
		} else {
			throw new UnsupportedOperationException("Invalid ApplicationStatus for assignment of Verifier");
		}
		return applicationsList.map(app -> modelMapper.map(app, DocumentApplicationResponse.class));
	}

	@Override
	@Transactional
	public DocumentApplicationResponse approveApplication(Long applicationId, String remarks) {
		DocumentApplication application = getApplicationById(applicationId);
		User verifier = securityUtils.getCurrentUser();
		if (verifier.getDesignation() == User.Designation.JUNIOR_VERIFIER
				&& application.getCurrentDesk().equals("DESK_1")) {
			application.setCurrentDesk("DESK_2");
//			application.setStatus(DocumentApplication.ApplicationStatus.UNDER_REVIEW);
			
		} else if (verifier.getDesignation() == User.Designation.SENIOR_VERIFIER
				&& application.getCurrentDesk().equals("DESK_2")) {
			application.setCurrentDesk("UNDER_CERTIFICATE_GENERATION");
			application.setStatus(DocumentApplication.ApplicationStatus.APPROVED);
		}
		application.setResolvedDate(LocalDateTime.now());
		application.setApprovedBy(verifier);
		DocumentApplication reviewedApplication = documentRepository.save(application);
		if (application.getCurrentDesk().equals("UNDER_CERTIFICATE_GENERATION")) {
			try {
			    byte[] pdf = pdfService.generateCertificate(reviewedApplication);
			    application.setCertificatePdf(pdf); // if you decide to save it
			} catch (IOException e) {
			    auditService.logActivity("PDF_GENERATION_FAILED", "Failed for Application " + applicationId);
			    throw new RuntimeException("Failed to generate certificate PDF", e);
			}
		}
		auditService.logActivity("DOCUMENT_APPROVED", String.format("Application %d approved by %s. Remarks: %s",
				applicationId, verifier.getEmail(), remarks));
		return modelMapper.map(reviewedApplication, DocumentApplicationResponse.class);
	}

	@Override
	@Transactional
	public DocumentApplicationResponse rejectApplication(Long applicationId, String remarks) {
		DocumentApplication application = getApplicationById(applicationId);
		User currentUser = securityUtils.getCurrentUser();
		application.setCurrentDesk("APPLICANT");
		application.setStatus(DocumentApplication.ApplicationStatus.REJECTED);
		application.setResolvedDate(LocalDateTime.now());
		application.setRejectionReason(remarks);
		DocumentApplication rejectedApplication = documentRepository.save(application);
		auditService.logActivity("DOCUMENT_REJECTED", String.format("Application %d rejected by %s. Remarks: %s",
				applicationId, currentUser.getEmail(), remarks));
		return modelMapper.map(rejectedApplication, DocumentApplicationResponse.class);
	}

	@Override
	@Transactional
	public DocumentApplicationResponse requestChanges(Long applicationId, String remarks) {
		DocumentApplication application = getApplicationById(applicationId);
		User currentUser = securityUtils.getCurrentUser();
		application.setStatus(DocumentApplication.ApplicationStatus.CHANGES_REQUESTED);
		application.setCurrentDesk("APPLICANT");
		application.setRejectionReason(remarks);
		DocumentApplication savedApp = documentRepository.save(application);

		auditService.logActivity("CHANGES_REQUESTED",
				String.format("Changes requested for application %d by %s. Remarks: %s", applicationId,
						currentUser.getEmail(), remarks));

		return modelMapper.map(savedApp, DocumentApplicationResponse.class);
	}


	@Override
	@Transactional(readOnly = true)
	public VerificationStatsResponse getVerificationStats() {
		User currentUser = securityUtils.getCurrentUser();
//        long totalAssigned = documentRepository.countByAssignedVerifiersContaining(currentUser);
		long pending = documentRepository.countByStatus(ApplicationStatus.PENDING);
		long applicationsOnDESK1 = documentRepository.countByCurrentDesk("DESK_1");
		long applicationOnDESK2 = documentRepository.countByCurrentDesk("DESK_2");
		long approved = documentRepository.countByStatus(ApplicationStatus.APPROVED);
		long totalAppliedApplications = documentRepository.count();
		long rejected = documentRepository.countByStatus(ApplicationStatus.REJECTED);
		return VerificationStatsResponse.builder().totalApplied(totalAppliedApplications)
				.countOnDesk1(applicationsOnDESK1).countOnDesk2(applicationOnDESK2).pending(pending).approved(approved)
				.rejected(rejected).build();
	}

	
	// hepler method
	private DocumentApplication getApplicationById(Long id) {
		return documentRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Application not found"));
	}

	@Override
	public ResponseEntity<Page<DocumentApplicationResponse>> getApprovedApplicationsByVerifier(Pageable pageable,
			long verifierId) {
		User verifier = userRepository.findById(verifierId)
	            .orElseThrow(() -> new UserNotFoundException("Verifier not found with id: " + verifierId));
	    Page<DocumentApplication> applicationsPage = documentRepository.findByApprovedBy(verifier, pageable);
	    Page<DocumentApplicationResponse> responsePage = applicationsPage.map(application ->
	            modelMapper.map(application, DocumentApplicationResponse.class)
	    );
	    return ResponseEntity.ok(responsePage);
	}

	

}