//package com.sunbeam.service.impl;
//
//import com.sunbeam.exception.*;
//import com.sunbeam.model.*;
//import com.sunbeam.repository.DocumentApplicationRepository;
//import com.sunbeam.repository.UserRepository;
//import com.sunbeam.repository.VerificationDeskAssignmentRepository;
//import com.sunbeam.service.VerificationWorkflowService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//public class VerificationWorkflowServiceImpl implements VerificationWorkflowService {
//
//    private final DocumentApplicationRepository appRepository;
//    private final UserRepository userRepository;
//    private final VerificationDeskAssignmentRepository deskAssignmentRepository;
//    
//    @Override
//    @Transactional
//    public void assignToVerificationDesk(DocumentApplication application) {
//        // Find available verifiers for the first desk
//        List<User> availableVerifiers = userRepository.findAvailableVerifiers();
//        
//        if (!availableVerifiers.isEmpty()) {
//            // Assign to first available verifier
//            User verifier = availableVerifiers.get(0);
//            
//            VerificationDeskAssignment assignment = new VerificationDeskAssignment();
//            assignment.setApplication(application);
//            assignment.setVerifier(verifier);
//            assignment.setDeskLevel("DESK_1");
//            
//            deskAssignmentRepository.save(assignment);
//            
//            // Add to application's assigned verifiers
//            application.getAssignedVerifiers().add(verifier);
//            appRepository.save(application);
//        }
//    }
//
//
//    @Override
//    @Transactional
//    public void moveToNextDesk(DocumentApplication application) {
//        // Current desk logic
//        String currentDesk = application.getCurrentDesk();
//        
//        if ("DESK_1".equals(currentDesk)) {
//            // Move to Desk 2
//            application.setCurrentDesk("DESK_2");
//            appRepository.save(application);
//            
//            // Assign to next available verifier
//            List<User> availableVerifiers = userRepository.findAvailableVerifiers();
//            if (!availableVerifiers.isEmpty()) {
//                User verifier = availableVerifiers.get(0);
//                
//                VerificationDeskAssignment assignment = new VerificationDeskAssignment();
//                assignment.setApplication(application);
//                assignment.setVerifier(verifier);
//                assignment.setDeskLevel("DESK_2");
//                
//                deskAssignmentRepository.save(assignment);
//                
//                application.getAssignedVerifiers().add(verifier);
//                appRepository.save(application);
//            }
//        } else if ("DESK_2".equals(currentDesk)) {
//            // Final approval - generate certificate
//            application.setStatus(DocumentApplication.ApplicationStatus.APPROVED);
//            application.setResolvedDate(LocalDateTime.now());
//            appRepository.save(application);
//            
//            // Generate certificate would happen automatically here
//        }
//    }
//
//    @Override
//    public List<DocumentApplication> getApplicationsByDesk(String deskLevel) {
//        return appRepository.findByCurrentDesk(deskLevel);
//    }
//
//    @Override
//    @Transactional
//    public void finalizeVerification(DocumentApplication application, boolean approved, String remarks) {
//        if(approved && "DESK_2".equals(application.getCurrentDesk())) {
//            application.setStatus(DocumentApplication.ApplicationStatus.APPROVED);
//        } else if(!approved) {
//            application.setStatus(DocumentApplication.ApplicationStatus.REJECTED);
//            application.setRejectionReason(remarks);
//        } else {
//            throw new WorkflowException("Invalid verification state");
//        }
//        
//        application.setResolvedDate(LocalDateTime.now());
//        appRepository.save(application);
//    }
//}