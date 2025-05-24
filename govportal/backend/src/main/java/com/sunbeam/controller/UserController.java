package com.sunbeam.controller;

import com.sunbeam.dto.request.UserUpdateRequest;
import com.sunbeam.dto.response.UserResponse;
import com.sunbeam.model.User;
import com.sunbeam.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser() {
        return ResponseEntity.ok(userService.getCurrentUser());
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateProfile(
            @Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateUserProfile(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserProfile(id));
    }
}