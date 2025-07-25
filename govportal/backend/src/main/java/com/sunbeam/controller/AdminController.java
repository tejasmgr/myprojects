package com.sunbeam.controller;

import com.sunbeam.dto.request.CreateVerifierRequest;
import com.sunbeam.dto.request.RegisterRequest;
import com.sunbeam.dto.response.AdminStatsResponse;
import com.sunbeam.dto.response.AuthResponse;
import com.sunbeam.dto.response.DocumentApplicationResponse;
import com.sunbeam.dto.response.ErrorResponse;
import com.sunbeam.dto.response.UserResponse;
import com.sunbeam.exception.DatabaseOperationException;
import com.sunbeam.exception.EmailAlreadyExistsException;
import com.sunbeam.exception.UserNotFoundException;
import com.sunbeam.model.DocumentApplication;
import com.sunbeam.model.User;
import com.sunbeam.service.AdminService;
import com.sunbeam.service.UserService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
	@Autowired
	private final UserService userService;
	
	
	@GetMapping("/user/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserProfile(id));
    }

    @PostMapping("/create-verifier")
    public ResponseEntity<?> createVerifier(
            @Valid @RequestBody CreateVerifierRequest request
    ) {
    	try {
    		 return ResponseEntity.ok(adminService.createVerifierAccount(request));
		}
    	catch (EmailAlreadyExistsException e) {
	        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("EMAIL_EXISTS", "Email is Already Registered"));
	    }
    	catch (DatabaseOperationException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("DATABASE_ERROR", "Database error occurred. Please try again."));
		}
    	
    }
    
    
    
    
    
    @GetMapping("/all-verifiers")
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
    
//    @GetMapping("/all-applications")
//    public ResponseEntity<Page<DocumentApplication>> getAllApplications(
//    		@PageableDefault(size = 20) Pageable pageable   ,
//            @RequestParam(required = false) String status
//    ) {
//        try {
//        	return ResponseEntity.ok(adminService.getAllApplicationsWithStatus(status,pageable));
//		} catch (DatabaseOperationException e) {
//			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//		}
//    }
//    
    
    

    @GetMapping("/applications")
    public ResponseEntity<Page<DocumentApplication>> getAllApplicationsWithStatus(
    		@PageableDefault(size = 20) Pageable pageable   ,
            @RequestParam(required = false) String status
    ) {
        try {
        	return ResponseEntity.ok(adminService.getAllApplicationsWithStatus(status,pageable));
		} catch (DatabaseOperationException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
    }
    
    @GetMapping("/applications/desk1")
    public ResponseEntity<Page<DocumentApplication>> getAllApplicationsOnDesk1(
    		@PageableDefault(size = 20) Pageable pageable          
    ) {
        try {
        	
        	return ResponseEntity.ok(adminService.getAllApplicationsOnDesk2("DESK_1",pageable));
		} catch (DatabaseOperationException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
    }
    
    @GetMapping("/applications/desk2")
    public ResponseEntity<Page<DocumentApplication>> getAllApplicationsOnDesk2(
    		@PageableDefault(size = 20) Pageable pageable 
    ) {
        try {
        	
        	return ResponseEntity.ok(adminService.getAllApplicationsOnDesk2("DESK_2",pageable));
		} catch (DatabaseOperationException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
    }
    
    
    
    
    
}