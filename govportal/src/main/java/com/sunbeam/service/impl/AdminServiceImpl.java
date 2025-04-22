package com.sunbeam.service.impl;

import com.sunbeam.dto.request.CreateVerifierRequest;
import com.sunbeam.dto.response.AdminStatsResponse;
import com.sunbeam.dto.response.UserResponse;
import com.sunbeam.exception.*;
import com.sunbeam.model.*;
import com.sunbeam.repository.DocumentApplicationRepository;
import com.sunbeam.repository.UserRepository;
import com.sunbeam.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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

    @Override
    @Transactional
    public UserResponse createVerifierAccount(CreateVerifierRequest request) {
        if(userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already registered");
        }

        User verifier = User.builder()
                .firstName(request.getFullName().split(" ")[0])
                .lastName(request.getFullName().split(" ")[1])
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(User.Role.VERIFIER)
                .enabled(true)
                .build();

        User savedUser = userRepository.save(verifier);
        return mapToUserResponse(savedUser);
    }

    @Override
    @Transactional
    public void deleteVerifierAccount(Long verifierId) {
        User verifier = userRepository.findById(verifierId)
                .orElseThrow(() -> new UserNotFoundException("Verifier not found"));
        
        if(verifier.getRole() != User.Role.VERIFIER) {
            throw new IllegalArgumentException("User is not a verifier");
        }
        
        userRepository.delete(verifier);
    }

    @Override
    @Transactional
    public UserResponse toggleUserBlockStatus(Long userId, boolean block) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        
        user.setBlocked(block);
        User updatedUser = userRepository.save(user);
        return mapToUserResponse(updatedUser);
    }

    @Override
    public Page<UserResponse> getAllCitizens(Pageable pageable) {
        Page<User> citizens = userRepository.findByRole(User.Role.CITIZEN, pageable);
        return citizens.map(this::mapToUserResponse);
    }

    @Override
    public AdminStatsResponse getSystemStatistics() {
        return AdminStatsResponse.builder()
                .totalCitizens(userRepository.countByRole(User.Role.CITIZEN))
                .activeVerifiers(userRepository.countByRoleAndEnabledTrue(User.Role.VERIFIER))
                .pendingApplications(appRepository.countByStatus(DocumentApplication.ApplicationStatus.PENDING))
                .approvedApplications(appRepository.countByStatus(DocumentApplication.ApplicationStatus.APPROVED))
                .blockedAccounts(userRepository.countByBlockedTrue())
                .build();
    }

    @Override
    public List<DocumentApplication> getAllApplications(String status) {
        if(status == null || status.isEmpty()) {
            return appRepository.findAll();
        }
        return appRepository.findByStatus(DocumentApplication.ApplicationStatus.valueOf(status.toUpperCase()));
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFirstName() + " " + user.getLastName())
                .email(user.getEmail())
                .role(user.getRole())
                .blocked(user.isBlocked())
                .build();
    }
}