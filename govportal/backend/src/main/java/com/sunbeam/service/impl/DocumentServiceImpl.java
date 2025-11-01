package com.sunbeam.service.impl;

import com.sunbeam.dto.request.DocumentApplicationRequest;
import com.sunbeam.dto.response.DocumentApplicationDetailsResponse;
import com.sunbeam.dto.response.DocumentApplicationResponse;
import com.sunbeam.dto.response.DocumentProofResponse;
import com.sunbeam.exception.*;
import com.sunbeam.model.*;
import com.sunbeam.repository.DocumentApplicationRepository;
import com.sunbeam.repository.DocumentProofRepository;
import com.sunbeam.security.SecurityUtils;
import com.sunbeam.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentApplicationRepository documentRepository;
    private final SecurityUtils securityUtils;
    private final ModelMapper modelMapper;
    private final DocumentProofRepository documentProofRepository;
    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    private static final Logger logger = LoggerFactory.getLogger(DocumentServiceImpl.class);

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DocumentApplication submitApplication(User applicant, DocumentApplicationRequest request, List<MultipartFile> files) {
        DocumentApplication application = DocumentApplication.builder()
                .applicant(applicant)
                .documentType(DocumentApplication.DocumentType.fromString(request.getDocumentType()))
                .purpose(request.getPurpose())
                .formData(request.getFormData())
                .status(DocumentApplication.ApplicationStatus.PENDING)
                .submissionDate(LocalDateTime.now())
                .currentDesk("DESK_1")
                .build();

        application = documentRepository.save(application);

        List<DocumentProof> documentProofs = new ArrayList<>();
        try {
            for (MultipartFile file : files) {
                String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                String objectKey = "documents/" + fileName;

                // Upload file to AWS S3
                s3Client.putObject(
                        PutObjectRequest.builder()
                                .bucket(bucketName)
                                .key(objectKey)
                                .contentType(file.getContentType())
                                .acl(ObjectCannedACL.PRIVATE) // set ACL as needed
                                .build(),
                        RequestBody.fromInputStream(file.getInputStream(), file.getSize())
                );

                // Save metadata in DocumentProof
                DocumentProof documentProof = DocumentProof.builder()
                        .application(application)
                        .fileName(file.getOriginalFilename())
                        .filePath(objectKey)
                        .contentType(file.getContentType())
                        .build();

                documentProofRepository.save(documentProof);
                documentProofs.add(documentProof);
            }

            application.setDocumentProofs(documentProofs);
            documentRepository.save(application);

            logger.info("Document application submitted successfully for user: {}", applicant.getEmail());
            return application;

        } catch (IOException e) {
            logger.error("Error uploading file to S3: {}", e.getMessage(), e);
            throw new FileStorageException("Failed to upload files to S3.");
        } catch (Exception e) {
            logger.error("Unexpected error submitting application: {}", e.getMessage(), e);
            throw new DatabaseOperationException("Failed to submit the application. Please try again.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentApplicationResponse> getAllApplications(Pageable pageable) {
        try {
            User currentUser = securityUtils.getCurrentUser();
            Page<DocumentApplication> applicationsList = documentRepository.findByApplicant(currentUser, pageable);
            return applicationsList.map(app -> modelMapper.map(app, DocumentApplicationResponse.class));
        } catch (Exception e) {
            logger.error("Error fetching all applications: {}", e.getMessage(), e);
            throw new DatabaseOperationException("Unable to fetch applications.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentApplicationResponse getApplicationById(Long id) {
        DocumentApplication application = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
        return modelMapper.map(application, DocumentApplicationResponse.class);
    }

    @Override
    @Transactional
    public byte[] getCertificatePdf(Long applicationId) {
        DocumentApplication application = documentRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with ID: " + applicationId));

        if (application.getStatus() != DocumentApplication.ApplicationStatus.APPROVED) {
            throw new IllegalStateException("Certificate can only be downloaded for approved applications.");
        }

        return application.getCertificatePdf();
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
                .approvedByUserId(application.getApprovedBy() != null ? application.getApprovedBy().getId() : null)
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
        User currentUser = securityUtils.getCurrentUser();
        Page<DocumentApplication> applicationsList = documentRepository.findByApplicantAndStatus(
                currentUser, DocumentApplication.ApplicationStatus.APPROVED, pageable);
        return applicationsList.map(app -> modelMapper.map(app, DocumentApplicationResponse.class));
    }

    @Override
    public Resource viewDocumentProof(Long documentProofId) throws IOException {
        DocumentProof documentProof = documentProofRepository.findById(documentProofId)
                .orElseThrow(() -> new ResourceNotFoundException("DocumentProof not found with ID: " + documentProofId));

        try {
            GetObjectResponse response;
            byte[] fileContent = s3Client.getObject(
                    GetObjectRequest.builder()
                            .bucket(bucketName)
                            .key(documentProof.getFilePath())
                            .build(),
                    software.amazon.awssdk.core.sync.ResponseTransformer.toBytes()
            ).asByteArray();

            return new ByteArrayResource(fileContent) {
                @Override
                public String getFilename() {
                    return documentProof.getFileName();
                }
            };

        } catch (S3Exception e) {
            logger.error("Error retrieving file from S3: {}", e.getMessage(), e);
            throw new IOException("Failed to retrieve file from S3: " + documentProof.getFileName(), e);
        }
    }

    public String getDocumentProofContentType(Long documentProofId) {
        DocumentProof documentProof = documentProofRepository.findById(documentProofId)
                .orElseThrow(() -> new ResourceNotFoundException("DocumentProof not found with ID: " + documentProofId));
        return documentProof.getContentType();
    }
}
