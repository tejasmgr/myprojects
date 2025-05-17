package com.sunbeam.controller;

import com.sunbeam.dto.request.CreateVerifierRequest;
import com.sunbeam.dto.request.RegisterRequest;
import com.sunbeam.dto.response.AdminStatsResponse;
import com.sunbeam.dto.response.AuthResponse;
import com.sunbeam.dto.response.UserResponse;
import com.sunbeam.exception.DatabaseOperationException;
import com.sunbeam.exception.EmailAlreadyExistsException;
import com.sunbeam.exception.UserNotFoundException;
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
    	try {
    		 return ResponseEntity.ok(adminService.createVerifierAccount(request));
		}
    	catch (EmailAlreadyExistsException e) {
	        return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
	    }
    	catch (DatabaseOperationException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
    }
    
    @GetMapping("/verifiers")
    public ResponseEntity<Page<UserResponse>> getAllVerifiers(@PageableDefault(size = 20) Pageable pageable){
    	try {
    		return ResponseEntity.ok(adminService.getAllVerifiers(pageable));
		} catch (DatabaseOperationException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
    	
    }
    
    @DeleteMapping("/delete-verifier/{id}")
    public ResponseEntity<Void> deleteVerifier(@PathVariable Long id) {
        try {
        	adminService.deleteVerifierAccount(id);
            return ResponseEntity.noContent().build();
		} catch (UserNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}catch (DatabaseOperationException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
    }

    @PutMapping("/block-user/{userId}")
    public ResponseEntity<UserResponse> toggleUserBlockStatus(
            @PathVariable Long userId,
            @RequestParam boolean block
    ) {
    	try {
    		return ResponseEntity.ok(adminService.toggleUserBlockStatus(userId, block));
		}catch (UserNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}catch (DatabaseOperationException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
    }

    @GetMapping("/citizens")
    public ResponseEntity<Page<UserResponse>> getAllCitizens(
            @PageableDefault(size = 20) Pageable pageable
    ) {
    	try {
    		return ResponseEntity.ok(adminService.getAllCitizens(pageable));
		} catch (DatabaseOperationException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
        
    }

    @GetMapping("/stats")
    public ResponseEntity<AdminStatsResponse> getSystemStats() {
        try {
        	return ResponseEntity.ok(adminService.getSystemStatistics());
		} catch (DatabaseOperationException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
    }

    @GetMapping("/applications")
    public ResponseEntity<List<DocumentApplication>> getAllApplications(
            @RequestParam(required = false) String status
    ) {
        try {
        	return ResponseEntity.ok(adminService.getAllApplications(status));
		} catch (DatabaseOperationException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
    }
}