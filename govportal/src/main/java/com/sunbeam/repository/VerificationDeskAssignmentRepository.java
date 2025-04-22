package com.sunbeam.repository;

import com.sunbeam.model.VerificationDeskAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VerificationDeskAssignmentRepository extends JpaRepository<VerificationDeskAssignment, Long> {
    List<VerificationDeskAssignment> findByDeskLevel(String deskLevel);
    List<VerificationDeskAssignment> findByVerifier_Id(Long verifierId);
}