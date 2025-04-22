package com.sunbeam.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.sunbeam.exception.InvalidDocumentTypeException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class DocumentApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User citizen;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentType documentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status = ApplicationStatus.PENDING;

    @Column(nullable = false)
    private String purpose;

    @ElementCollection
    @CollectionTable(name = "application_documents", joinColumns = @JoinColumn(name = "application_id"))
    @Column(name = "document_path")
    private List<String> documentPaths = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "application_verifiers",
        joinColumns = @JoinColumn(name = "application_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> assignedVerifiers = new ArrayList<>();
    

    private String rejectionReason;
    private String currentDesk; // "DESK_1", "DESK_2" etc
    
    @ManyToOne
    @JoinColumn(name = "approved_by_user_id")
    private User approvedBy;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime appliedDate;

    @LastModifiedDate
    private LocalDateTime lastUpdatedDate;

    private LocalDateTime resolvedDate;

    public enum DocumentType {
        INCOME_CERTIFICATE,
        CASTE_CERTIFICATE,
        DOMICILE_CERTIFICATE,
        BIRTH_CERTIFICATE;
        
        public static DocumentType fromString(String text) {
            try {
                return DocumentType.valueOf(text.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new InvalidDocumentTypeException("Invalid document type: " + text);
            }
    }}

    public enum ApplicationStatus {
        PENDING,
        UNDER_REVIEW,
        APPROVED,
        REJECTED,
        REAPPLIED, CHANGES_REQUESTED
    }

    // Helper method to add document path
    public void addDocumentPath(String path) {
        this.documentPaths.add(path);
    }

    public User getApprovedBy() {
        return this.approvedBy;
    }
    
   
    
    
    
}