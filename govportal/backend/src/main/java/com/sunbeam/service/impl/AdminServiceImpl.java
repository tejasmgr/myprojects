package com.sunbeam.service.impl;

import com.sunbeam.dto.request.CreateVerifierRequest;
import com.sunbeam.dto.response.AdminStatsResponse;
import com.sunbeam.dto.response.DocumentApplicationResponse;
import com.sunbeam.dto.response.UserResponse;
import com.sunbeam.exception.*;
import com.sunbeam.model.*;
import com.sunbeam.model.User.Designation;
import com.sunbeam.repository.DocumentApplicationRepository;
import com.sunbeam.repository.UserRepository;
import com.sunbeam.service.AdminService;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final DocumentApplicationRepository appRepository;
    private final PasswordEncoder passwordEncoder;
    private static final Logger logger = LoggerFactory.getLogger(AdminServiceImpl.class);
    
    
    @Override
    @Transactional
    public UserResponse createVerifierAccount(CreateVerifierRequest request) {
        if(userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already registered : "+ request.getEmail());
        }
        Designation designation = Designation.valueOf(request.getDesignation().toUpperCase());
        User verifier = User.builder()
                .firstName(request.getFullName().split(" ")[0])
                .lastName(request.getFullName().split(" ")[1])
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(User.Role.VERIFIER)
                .designation(designation)
                .enabled(true)
                .address(request.getAddress())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .aadharNumber(request.getAadharNumber())
                .build();

        System.out.println("Designation : "+ verifier.getDesignation()+ "Enabled : "+ verifier.isEnabled());
        User savedUser = userRepository.save(verifier);
//       System.out.println("Enabled Status of Persisted Entity"+savedUser.isEnabled()); 
        return mapToUserResponse(savedUser);
    }
    
    @Override
	public Page<UserResponse> getAllVerifiers(Pageable pageable) {
		 Page<User> verifiers = userRepository.findByRole(User.Role.VERIFIER, pageable);
	        return verifiers.map(this::mapToUserResponse);
	}

    @Override
    @Transactional
    public void deleteVerifierAccount(Long verifierId) {
    	System.out.println("insode service layer");
        User verifier = userRepository.findById(verifierId)
                .orElseThrow(() -> new UserNotFoundException("Verifier not found with ID : "+ verifierId));
        
        if(verifier.getRole() != User.Role.VERIFIER) {
            throw new IllegalArgumentException("User is not a verifier" + verifierId);
        }       
        userRepository.delete(verifier);
        logger.info("Deleted verifier account with ID: {}", verifierId);
    }
    
    @Override
    public Page<UserResponse> getAllCitizens(Pageable pageable) {
       try {
       Page<User> citizens = userRepository.findByRole(User.Role.CITIZEN, pageable);
        return citizens.map(this::mapToUserResponse);}
       catch (Exception e) {
    	   logger.error("Error fetching citizens: {}", e.getMessage());
    	   throw new DatabaseOperationException("Error fetching citizens", e);
	}
    }

    @Override
    @Transactional
    public UserResponse toggleUserBlockStatus(Long userId, boolean block) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with Id : "+userId));
      
        user.setBlocked(block);
        User updatedUser = userRepository.save(user);
        logger.info("User  block status changed for ID: {} to {}", userId, block);
        return mapToUserResponse(updatedUser);
    }



    @Override
    public AdminStatsResponse getSystemStatistics() {
    	try {return AdminStatsResponse.builder()
                .totalCitizens(userRepository.countByRole(User.Role.CITIZEN))
                .activeVerifiers(userRepository.countByRoleAndEnabledTrue(User.Role.VERIFIER))
                .pendingApplications(appRepository.countByStatus(DocumentApplication.ApplicationStatus.PENDING))
                .approvedApplications(appRepository.countByStatus(DocumentApplication.ApplicationStatus.APPROVED))
                .blockedAccounts(userRepository.countByBlockedTrue())
                .build();
			
		} catch (Exception e) {
			logger.error("Error fetching system statistics: {}", e.getMessage());
			throw new DatabaseOperationException("Error fetching System Statistics",e);
			
		}
        
    }

    @Override
    public Page<DocumentApplication> getAllApplicationsWithStatus(String status, Pageable pageable) {
        if(status == null || status.isEmpty()) {
            return appRepository.findAll(pageable);
        }
        return appRepository.findByStatus(DocumentApplication.ApplicationStatus.valueOf(status.toUpperCase()),pageable);
    }
    

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFirstName() + " " + user.getLastName())
                .email(user.getEmail())
                .role(user.getRole())
                .designation(user.getDesignation())
                .updatedAt(user.getUpdatedAt())
                .blocked(user.isBlocked())
                .enabled(user.isEnabled())
                .build();
    }

	@Override
	public Page<DocumentApplication> getAllApplicationsOnDesk1(String currentDesk, Pageable pageable) {
		return appRepository.findByCurrentDesk(currentDesk, pageable);
		
	}
	
	@Override
	public Page<DocumentApplication> getAllApplicationsOnDesk2(String currentDesk, Pageable pageable) {
		return appRepository.findByCurrentDesk(currentDesk, pageable);
		
	}

	
}