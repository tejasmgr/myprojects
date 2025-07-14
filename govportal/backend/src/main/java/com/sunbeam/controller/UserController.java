package com.sunbeam.controller;

import com.sunbeam.dto.request.ChangePasswordRequest;
import com.sunbeam.dto.request.UserUpdateRequest;
import com.sunbeam.dto.response.UserResponse;
import com.sunbeam.model.User;
import com.sunbeam.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    
    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getCurrentUser() {
        return ResponseEntity.ok(userService.getCurrentUser());
    }

    @PutMapping("/profile/update")
    public ResponseEntity<?> updateProfile(
            @Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateUserProfile(request));
    }

    @GetMapping("/profile/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserProfile(id));
    }
    
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody @Valid ChangePasswordRequest request) {
    	userService.changePassword(request);
    	return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }
}