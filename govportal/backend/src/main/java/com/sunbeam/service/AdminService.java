package com.sunbeam.service;

import com.sunbeam.dto.request.CreateVerifierRequest;
import com.sunbeam.dto.request.RegisterRequest;
import com.sunbeam.dto.response.AdminStatsResponse;
import com.sunbeam.dto.response.DocumentApplicationResponse;
import com.sunbeam.dto.response.UserResponse;
import com.sunbeam.model.DocumentApplication;
import com.sunbeam.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdminService {
    UserResponse createVerifierAccount(CreateVerifierRequest request);
    void deleteVerifierAccount(Long verifierId);
    UserResponse toggleUserBlockStatus(Long userId, boolean block);
    Page<UserResponse> getAllCitizens(Pageable pageable);
    AdminStatsResponse getSystemStatistics();
    Page<DocumentApplication> getAllApplicationsWithStatus(String status,Pageable pageable);
	Page<UserResponse> getAllVerifiers(Pageable pageable);
	Page<DocumentApplication> getAllApplicationsOnDesk1(String currentDesk,Pageable pageable);
	Page<DocumentApplication> getAllApplicationsOnDesk2(String currentDesk,Pageable pageable);
	
}