package com.sunbeam.service;

import com.sunbeam.dto.request.UserUpdateRequest;
import com.sunbeam.dto.response.UserResponse;
import com.sunbeam.model.ApiResponse;
import com.sunbeam.model.User;

import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User getCurrentUser();
    UserResponse getUserProfile(Long userId);
    List<User> getAllVerifiers();
    void updateUserProfile(User user);
	UserResponse getCurrentUserProfile();
	ApiResponse updateUserProfile(@Valid UserUpdateRequest request);
}