package com.sunbeam.repository;

import com.sunbeam.model.DocumentApplication;
import com.sunbeam.model.DocumentApplication.ApplicationStatus;
import com.sunbeam.model.DocumentApplication.DocumentType;
import com.sunbeam.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DocumentApplicationRepository extends JpaRepository<DocumentApplication, Long> {
    Page<DocumentApplication> findByApplicant(User applicant,Pageable pageable);
    Page<DocumentApplication> findByApplicantAndStatus(User applicant, ApplicationStatus status,Pageable pageable);
    List<DocumentApplication> findByStatus(ApplicationStatus status);
    Page<DocumentApplication> findByCurrentDesk(String deskLevel, Pageable pageable);
    long countByStatus(ApplicationStatus status);
    long count();
    long countByCurrentDesk(String deskLevel);
    Optional<DocumentApplication>	 findById(long id);
    
    @Query("SELECT da FROM DocumentApplication da LEFT JOIN FETCH da.documentProofs WHERE da.id = :applicationId")
    Optional<DocumentApplication> findByIdWithProofs(@Param("applicationId") Long applicationId);
    
    Page<DocumentApplication> findByApprovedBy(User verifier, Pageable pageable);

    @Query("SELECT da FROM DocumentApplication da WHERE da.status = :status ORDER BY da.resolvedDate DESC")
    List<DocumentApplication> findTopNByStatusOrderByResolvedDateDesc(@Param("status") ApplicationStatus status, org.springframework.data.domain.Pageable pageable);

    // Corrected method - removes toUpperCase() call
    @Query("SELECT da FROM DocumentApplication da " +
            "WHERE (:documentType IS NULL OR da.documentType = :documentType) " +
            "AND (:status IS NULL OR da.status = :status)")
    List<DocumentApplication> findByFilters(
            @Param("documentType") DocumentType documentType,
            @Param("status") ApplicationStatus status
    );
    
    


    @Query("SELECT da FROM DocumentApplication da WHERE da.approvedBy.id = :userId")
    List<DocumentApplication> findApprovedByUserId(@Param("userId") Long userId);
}