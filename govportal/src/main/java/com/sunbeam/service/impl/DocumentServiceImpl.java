package com.sunbeam.service.impl;

import com.sunbeam.dto.request.DocumentApplicationRequest;
import com.sunbeam.dto.response.DocumentApplicationResponse;
import com.sunbeam.exception.*;
import com.sunbeam.model.*;
import com.sunbeam.repository.DocumentApplicationRepository;
import com.sunbeam.repository.UserRepository;
import com.sunbeam.security.SecurityUtils;
import com.sunbeam.service.*;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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

    @Override
    @Transactional
    public DocumentApplicationResponse submitApplication(DocumentApplicationRequest request) {
        User currentUser = securityUtils.getCurrentUser();

        // Store documents and get paths
        List<String> documentPaths = request.getDocuments().stream()
                .map(file -> {
                    try {
                        return fileStorageService.storeFile(file);
                    } catch (IOException e) {
                        throw new FileStorageException("Failed to store document: " + e.getMessage());
                    }
                })
                .collect(Collectors.toList());

        DocumentApplication application = DocumentApplication.builder()
                .citizen(currentUser)
                .documentType(request.getDocumentType())
                .status(DocumentApplication.ApplicationStatus.PENDING)
                .documentPaths(documentPaths)
                .appliedDate(LocalDateTime.now())
                .purpose(request.getPurpose())
                .build();

        DocumentApplication savedApplication = documentRepository.save(application);
        workflowService.assignToVerificationDesk(savedApplication);

        auditService.logActivity(
            "DOCUMENT_SUBMIT",
            "Submitted " + request.getDocumentType() + " application"
        );

        return modelMapper.map(savedApplication, DocumentApplicationResponse.class);
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
        return documentRepository.findByCitizen(currentUser).stream()
                .map(app -> modelMapper.map(app, DocumentApplicationResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentApplicationResponse> getAllApplications(Pageable pageable) {
        return documentRepository.findAll(pageable)
                .map(app -> modelMapper.map(app, DocumentApplicationResponse.class));
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

        auditService.logDocumentStatusChange(
            applicationId,
            "DOCUMENT_APPROVED",
            "Approved by " + currentUser.getEmail() + " with remarks: " + remarks
        );

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

        auditService.logDocumentStatusChange(
            applicationId,
            "DOCUMENT_REJECTED",
            "Rejected with remarks: " + remarks
        );

        return modelMapper.map(savedApp, DocumentApplicationResponse.class);
    }

    @Override
    @Transactional
    public String uploadSupportingDocument(Long applicationId, MultipartFile file) {
        DocumentApplication application = documentRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        if (application.getStatus() != DocumentApplication.ApplicationStatus.PENDING) {
            throw new IllegalStateException("Cannot add documents to processed applications");
        }

        try {
            String filePath = fileStorageService.storeFile(file);
            application.getDocumentPaths().add(filePath);
            documentRepository.save(application);
            
            auditService.logActivity(
                "DOCUMENT_UPLOAD",
                "Added supporting document to application " + applicationId
            );
            
            return filePath;
        } catch (IOException e) {
            throw new FileStorageException("Failed to store document: " + e.getMessage());
        }
    }

    private void verifyAccessPermissions(DocumentApplication application) {
        User currentUser = securityUtils.getCurrentUser();
        
        if (!application.getCitizen().equals(currentUser) && 
            !application.getAssignedVerifiers().contains(currentUser) &&
            !currentUser.getRole().equals(User.Role.ADMIN)) {
            throw new UnauthorizedAccessException("You don't have permission to access this application");
        }
    }

	@Override
	public DocumentApplicationResponse reassignApplication(Long applicationId, Long newVerifierId) {
		// TODO Auto-generated method stub
		return null;
		}
	
}
