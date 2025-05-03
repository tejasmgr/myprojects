package com.sunbeam.service.impl;

import com.sunbeam.dto.response.DocumentApplicationDetailsResponse;
import com.sunbeam.dto.response.DocumentApplicationResponse;
import com.sunbeam.dto.response.DocumentProofResponse;
import com.sunbeam.dto.response.VerificationStatsResponse;
import com.sunbeam.exception.*;
import com.sunbeam.model.*;
import com.sunbeam.model.DocumentApplication.ApplicationStatus;
import com.sunbeam.repository.DocumentApplicationRepository;
import com.sunbeam.repository.UserRepository;
import com.sunbeam.security.SecurityUtils;
import com.sunbeam.service.AuditService;
import com.sunbeam.service.VerificationService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

	@Override
	public ResponseEntity<DocumentApplicationResponse> getDocumentApplicationById(Long id) {
		DocumentApplication application = documentRepository.getById(id);
	    DocumentApplicationResponse dto = modelMapper.map(application, DocumentApplicationResponse.class);
	    return ResponseEntity.ok(dto);
	}

	@Override
	public Page<DocumentApplicationResponse> getPendingApplications(Pageable pageable) {
		User verifier = securityUtils.getCurrentUser();
		Page<DocumentApplication> applicationsList = null;

		if (verifier.getDesignation() == User.Designation.JUNIOR_VERIFIER) {
			applicationsList = documentRepository.findByCurrentDesk("DESK_1", pageable);
		} else if (verifier.getDesignation() == User.Designation.SENIOR_VERIFIER) {
			applicationsList = documentRepository.findByCurrentDesk("DESK_2", pageable);
		} else {
			throw new UnsupportedOperationException("Invalid ApplicationStatus for assignment of Verifier");

		}
		return applicationsList.map(app -> modelMapper.map(app, DocumentApplicationResponse.class));
	}

	@Override
	@Transactional
	public DocumentApplicationResponse approveApplication(Long applicationId, String remarks) {
		DocumentApplication application = getApplicationById(applicationId);
		User currentUser = securityUtils.getCurrentUser();
		if (currentUser.getDesignation() == User.Designation.JUNIOR_VERIFIER
				&& application.getCurrentDesk() == "DESK_1") {
			application.setCurrentDesk("DESK_2");
			application.setStatus(DocumentApplication.ApplicationStatus.UNDER_REVIEW);
		} else if (currentUser.getDesignation() == User.Designation.SENIOR_VERIFIER
				&& application.getCurrentDesk() == "DESK_2") {
			application.setCurrentDesk("UNDER_CERTIFICATE_GENERATION");
			application.setStatus(DocumentApplication.ApplicationStatus.APPROVED);
		}
		application.setResolvedDate(LocalDateTime.now());
		application.setApprovedBy(currentUser);
		DocumentApplication reviewedApplication = documentRepository.save(application);

		auditService.logActivity("DOCUMENT_APPROVED", String.format("Application %d approved by %s. Remarks: %s",
				applicationId, currentUser.getEmail(), remarks));
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

//    @Override
//    @Transactional(readOnly = true)
//    public Page<DocumentApplicationResponse> getAssignedApplications(int page, int size) {
//    	User currentUser = securityUtils.getCurrentUser();
//    	PageRequest pageable = PageRequest.of(page, size, Sort.by("submissionDate").descending());
//    	Page<DocumentApplication> applicationPage = documentRepository.findByAssignedVerifiersContaining(currentUser, pageable);
//    	
//    	return applicationPage.map(app -> modelMapper.map(app, DocumentApplicationResponse.class));
//        
//                
//    }

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

	

}