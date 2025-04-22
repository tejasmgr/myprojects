package com.sunbeam.service.impl;

import com.sunbeam.exception.*;
import com.sunbeam.model.*;
import com.sunbeam.repository.DocumentApplicationRepository;
import com.sunbeam.repository.UserRepository;
import com.sunbeam.service.VerificationWorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VerificationWorkflowServiceImpl implements VerificationWorkflowService {

    private final DocumentApplicationRepository appRepository;
    private final UserRepository userRepository;
    
    @Override
    @Transactional
    public void assignToVerificationDesk(DocumentApplication application) {
        List<User> verifiers = userRepository.findAvailableVerifiers();
        
        if(verifiers.isEmpty()) {
            throw new NoAvailableVerifiersException("No verifiers available");
        }
        
        application.setAssignedVerifiers(verifiers.stream()
                .limit(3)
                .collect(Collectors.toList()));
        application.setCurrentDesk("DESK_1");
        appRepository.save(application);
    }

    @Override
    @Transactional
    public void moveToNextDesk(DocumentApplication application) {
        if(!"DESK_1".equals(application.getCurrentDesk())) {
            throw new WorkflowException("Application already in final desk");
        }
        
        application.setCurrentDesk("DESK_2");
        appRepository.save(application);
    }

    @Override
    public List<DocumentApplication> getApplicationsByDesk(String deskLevel) {
        return appRepository.findByCurrentDesk(deskLevel);
    }

    @Override
    @Transactional
    public void finalizeVerification(DocumentApplication application, boolean approved, String remarks) {
        if(approved && "DESK_2".equals(application.getCurrentDesk())) {
            application.setStatus(DocumentApplication.ApplicationStatus.APPROVED);
        } else if(!approved) {
            application.setStatus(DocumentApplication.ApplicationStatus.REJECTED);
            application.setRejectionReason(remarks);
        } else {
            throw new WorkflowException("Invalid verification state");
        }
        
        application.setResolvedDate(LocalDateTime.now());
        appRepository.save(application);
    }
}