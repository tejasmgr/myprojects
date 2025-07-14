package com.sunbeam.dto.response;

import com.sunbeam.model.DocumentApplication.ApplicationStatus;
import com.sunbeam.model.DocumentApplication.DocumentType;
import com.sunbeam.model.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class DocumentApplicationDetailsResponse {
    private Long id;
    private User applicant;
    private String applicantName;
    private String adhaarNumber;
    private String formData;
    private DocumentType documentType;
    private ApplicationStatus status;
    private String purpose;
    private String rejectionReason;
    private String currentDesk;
    private Long approvedByUserId; // Only include the ID to avoid circular dependencies
    private LocalDateTime submissionDate;
    private LocalDateTime lastUpdatedDate;
    private LocalDateTime resolvedDate;
    private Long applicantId; // Include applicant ID for reference
    private List<DocumentProofResponse> documentProofs;
}