package com.sunbeam.repository;

import com.sunbeam.dto.response.AuditLogResponse;
import com.sunbeam.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
	 // Basic paginated findAll with sorting
    Page<AuditLog> findAllByOrderByTimestampDesc(Pageable pageable);
    
    @Query("SELECT al FROM AuditLog al WHERE al.user.id = :userId")
    Page<AuditLog> findByUserId(@Param("userId") Long userId, Pageable pageable);

    // Filter by user ID with pagination
    Page<AuditLog> findByUserIdOrderByTimestampDesc(Long userId, Pageable pageable);

    // Filter by action type
    Page<AuditLog> findByActionContainingIgnoreCaseOrderByTimestampDesc(String action, Pageable pageable);

    // Date range filtering
    @Query("SELECT al FROM AuditLog al " +
           "WHERE al.timestamp BETWEEN :startDate AND :endDate " +
           "ORDER BY al.timestamp DESC")
    Page<AuditLog> findByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    // Combined filters
    @Query("SELECT al FROM AuditLog al " +
           "WHERE (:userId IS NULL OR al.user.id = :userId) " +
           "AND (:action IS NULL OR LOWER(al.action) LIKE LOWER(CONCAT('%', :action, '%'))) " +
           "AND (:startDate IS NULL OR al.timestamp >= :startDate) " +
           "AND (:endDate IS NULL OR al.timestamp <= :endDate) " +
           "ORDER BY al.timestamp DESC")
    Page<AuditLog> findByFilters(
            @Param("userId") Long userId,
            @Param("action") String action,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    // For dashboard statistics
    @Query("SELECT al.action, COUNT(al) FROM AuditLog al " +
           "WHERE al.timestamp >= :since " +
           "GROUP BY al.action")
    List<Object[]> countActionsSince(@Param("since") LocalDateTime since);

    // Bulk delete old logs (for cleanup jobs)
    @Query("DELETE FROM AuditLog al WHERE al.timestamp < :cutoffDate")
    void deleteOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);

	Page findAll(Pageable pageable);
}