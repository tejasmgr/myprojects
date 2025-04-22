package com.sunbeam.service.impl;

import com.sunbeam.dto.response.DocumentApplicationResponse;
import com.sunbeam.dto.response.VerificationStatsResponse;
import com.sunbeam.exception.*;
import com.sunbeam.model.*;
import com.sunbeam.repository.DocumentApplicationRepository;
import com.sunbeam.repository.UserRepository;
import com.sunbeam.security.SecurityUtils;
import com.sunbeam.service.AuditService;
import com.sunbeam.service.VerificationService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VerificationServiceImpl implements VerificationService {

    private final DocumentApplicationRepository documentRepository;
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;
    private final AuditService auditService;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public DocumentApplicationResponse assignToVerifier(Long applicationId, Long verifierId) {
        DocumentApplication application = getApplicationById(applicationId);
        User verifier = getUserById(verifierId);

        if (verifier.getRole() != User.Role.VERIFIER) {
            throw new InvalidOperationException("User is not a verifier");
        }

        application.getAssignedVerifiers().add(verifier);
        application.setStatus(DocumentApplication.ApplicationStatus.UNDER_REVIEW);
        DocumentApplication savedApp = documentRepository.save(application);

        auditService.logActivity(
            "VERIFICATION_ASSIGNED",
            String.format("Application %d assigned to verifier %s", 
                applicationId, verifier.getEmail())
        );

        return modelMapper.map(savedApp, DocumentApplicationResponse.class);
    }

    @Override
    @Transactional
    public DocumentApplicationResponse approveApplication(Long applicationId, String remarks) {
        DocumentApplication application = getApplicationById(applicationId);
        User currentUser = securityUtils.getCurrentUser();

        verifyVerifierAssignment(application, currentUser);

        application.setStatus(DocumentApplication.ApplicationStatus.APPROVED);
        application.setResolvedDate(LocalDateTime.now());
        application.setApprovedBy(currentUser);
        DocumentApplication savedApp = documentRepository.save(application);

        auditService.logActivity(
            "DOCUMENT_APPROVED",
            String.format("Application %d approved by %s. Remarks: %s", 
                applicationId, currentUser.getEmail(), remarks)
        );

        return modelMapper.map(savedApp, DocumentApplicationResponse.class);
    }

    @Override
    @Transactional
    public DocumentApplicationResponse rejectApplication(Long applicationId, String remarks) {
        DocumentApplication application = getApplicationById(applicationId);
        User currentUser = securityUtils.getCurrentUser();

        verifyVerifierAssignment(application, currentUser);

        application.setStatus(DocumentApplication.ApplicationStatus.REJECTED);
        application.setResolvedDate(LocalDateTime.now());
        application.setRejectionReason(remarks);
        DocumentApplication savedApp = documentRepository.save(application);

        auditService.logActivity(
            "DOCUMENT_REJECTED",
            String.format("Application %d rejected by %s. Remarks: %s", 
                applicationId, currentUser.getEmail(), remarks)
        );

        return modelMapper.map(savedApp, DocumentApplicationResponse.class);
    }

    @Override
    @Transactional
    public DocumentApplicationResponse requestChanges(Long applicationId, String remarks) {
        DocumentApplication application = getApplicationById(applicationId);
        User currentUser = securityUtils.getCurrentUser();

        verifyVerifierAssignment(application, currentUser);

        application.setStatus(DocumentApplication.ApplicationStatus.CHANGES_REQUESTED);
        application.setRejectionReason(remarks);
        DocumentApplication savedApp = documentRepository.save(application);

        auditService.logActivity(
            "CHANGES_REQUESTED",
            String.format("Changes requested for application %d by %s. Remarks: %s", 
                applicationId, currentUser.getEmail(), remarks)
        );

        return modelMapper.map(savedApp, DocumentApplicationResponse.class);
    }

//    @Override
//    @Transactional(readOnly = true)
//    public Page<DocumentApplicationResponse> getAssignedApplications(int page, int size) {
//        User currentUser = securityUtils.getCurrentUser();
//        PageRequest pageable = PageRequest.of(page, size, Sort.by("appliedDate").descending());
//        
//        return documentRepository.findByAssignedVerifiersContaining(currentUser, pageable)
//                .map(app -> modelMapper.map(app, DocumentApplicationResponse.class));
//    }

//    @Override
//    @Transactional(readOnly = true)
//    public VerificationStatsResponse getVerificationStats() {
//        User currentUser = securityUtils.getCurrentUser();
//        long totalAssigned = documentRepository.countByAssignedVerifiersContaining(currentUser);
//        long pending = documentRepository.countByAssignedVerifiersContainingAndStatus(
//            currentUser, DocumentApplication.ApplicationStatus.UNDER_REVIEW);
//        long approved = documentRepository.countByAssignedVerifiersContainingAndStatus(
//            currentUser, DocumentApplication.ApplicationStatus.APPROVED);
//
//        return VerificationStatsResponse.builder()
//                .totalAssigned(totalAssigned)
//                .pending(pending)
//                .approved(approved)
//                .build();
//    }
//
//    @Override
//    @Transactional
//    public DocumentApplicationResponse escalateToSeniorVerifier(Long applicationId, String reason) {
//        DocumentApplication application = getApplicationById(applicationId);
//        User currentUser = securityUtils.getCurrentUser();
//
//        verifyVerifierAssignment(application, currentUser);
//
//        // Find a senior verifier (logic depends on your business rules)
//        User seniorVerifier = userRepository.findSeniorVerifier()
//                .orElseThrow(() -> new ResourceNotFoundException("No senior verifier available"));
//
//        application.getAssignedVerifiers().add(seniorVerifier);
//        DocumentApplication savedApp = documentRepository.save(application);
//
//        auditService.logActivity(
//            "ESCALATED_TO_SENIOR",
//            String.format("Application %d escalated to %s. Reason: %s", 
//                applicationId, seniorVerifier.getEmail(), reason)
//        );
//
//        return modelMapper.map(savedApp, DocumentApplicationResponse.class);
//    }

    // Helper methods
    private DocumentApplication getApplicationById(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
    }

    private User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private void verifyVerifierAssignment(DocumentApplication application, User verifier) {
        if (!application.getAssignedVerifiers().contains(verifier)) {
            throw new UnauthorizedAccessException("You are not assigned to verify this application");
        }
    }
}