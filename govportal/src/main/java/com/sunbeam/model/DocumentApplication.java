package com.sunbeam.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.sunbeam.exception.InvalidDocumentTypeException;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "document_applications")
@EntityListeners(AuditingEntityListener.class)

public class DocumentApplication {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User applicant;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private DocumentType documentType;

	@Column(columnDefinition = "TEXT")
	private String formData;

	@OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference
	private List<DocumentProof> documentProofs;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ApplicationStatus status = ApplicationStatus.PENDING;

	@Column(nullable = false)
	private String purpose;



	private String rejectionReason;
	private String currentDesk; // "DESK_1", "DESK_2" etc

	@ManyToOne
	@JoinColumn(name = "approved_by_user_id")
	private User approvedBy;

	@CreatedDate
	@Column(updatable = false)
	private LocalDateTime submissionDate;

	@LastModifiedDate
	private LocalDateTime lastUpdatedDate;

	private LocalDateTime resolvedDate;

	@Lob
	@Column(name = "certificate_pdf", columnDefinition = "LONGBLOB")
	private byte[] certificatePdf;
	
	public enum DocumentType {
		INCOME, CASTE, DOMICILE, BIRTH;

		public static DocumentType fromString(String text) {
			try {
				return DocumentType.valueOf(text.toUpperCase());
			} catch (IllegalArgumentException e) {
				throw new InvalidDocumentTypeException("Invalid document type: " + text);
			}
		}
	}

	public enum ApplicationStatus {
		PENDING, UNDER_REVIEW, APPROVED, REJECTED, REAPPLIED, CHANGES_REQUESTED
	}
	

}