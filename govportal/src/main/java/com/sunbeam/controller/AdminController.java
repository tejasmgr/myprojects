package com.sunbeam.controller;

import com.sunbeam.dto.request.CreateVerifierRequest;
import com.sunbeam.dto.request.RegisterRequest;
import com.sunbeam.dto.response.AdminStatsResponse;
import com.sunbeam.dto.response.AuthResponse;
import com.sunbeam.dto.response.UserResponse;
import com.sunbeam.model.DocumentApplication;
import com.sunbeam.model.User;
import com.sunbeam.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
	@Autowired
    private final AdminService adminService;

    @PostMapping("/create-verifier")
    public ResponseEntity<UserResponse> createVerifier(
            @Valid @RequestBody CreateVerifierRequest request
    ) {
    	System.out.println("Inside Create Verifier Method from Admin COntroller");
        return ResponseEntity.ok(adminService.createVerifierAccount(request));
    }
    
    

    @DeleteMapping("/verifiers/{id}")
    public ResponseEntity<Void> deleteVerifier(@PathVariable Long id) {
        adminService.deleteVerifierAccount(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/block-user/{userId}")
    public ResponseEntity<UserResponse> toggleUserBlockStatus(
            @PathVariable Long userId,
            @RequestParam boolean block
    ) {
        return ResponseEntity.ok(adminService.toggleUserBlockStatus(userId, block));
    }

    @GetMapping("/citizens")
    public ResponseEntity<Page<UserResponse>> getAllCitizens(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(adminService.getAllCitizens(pageable));
    }

    @GetMapping("/stats")
    public ResponseEntity<AdminStatsResponse> getSystemStats() {
        return ResponseEntity.ok(adminService.getSystemStatistics());
    }

    @GetMapping("/applications")
    public ResponseEntity<List<DocumentApplication>> getAllApplications(
            @RequestParam(required = false) String status
    ) {
        return ResponseEntity.ok(adminService.getAllApplications(status));
    }
}