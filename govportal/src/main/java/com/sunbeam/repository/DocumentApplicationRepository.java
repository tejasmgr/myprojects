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

public interface DocumentApplicationRepository extends JpaRepository<DocumentApplication, Long> {
    List<DocumentApplication> findByCitizen(User citizen);
    List<DocumentApplication> findByAssignedVerifiersContaining(User verifier);
    List<DocumentApplication> findByStatus(ApplicationStatus status);
    List<DocumentApplication> findByCurrentDesk(String deskLevel);
    long countByStatus(ApplicationStatus status);

    @Query("SELECT da.status as status, COUNT(da) as count FROM DocumentApplication da GROUP BY da.status")
    List<Object[]> getCountByStatus();

    @Query("SELECT AVG(TIMESTAMPDIFF(MINUTE, da.appliedDate, da.resolvedDate)) " +
            "FROM DocumentApplication da WHERE da.status = 'APPROVED'")
    Double getAverageProcessingTime();

    // Corrected method with @Query
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

    List<DocumentApplication> findByApprovedBy(User user);

    @Query("SELECT da FROM DocumentApplication da WHERE da.approvedBy.id = :userId")
    List<DocumentApplication> findApprovedByUserId(@Param("userId") Long userId);
}