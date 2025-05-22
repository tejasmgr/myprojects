package com.sunbeam.service.impl;



import com.sunbeam.dto.request.UserUpdateRequest;
import com.sunbeam.dto.response.UserResponse;
import com.sunbeam.exception.ResourceNotFoundException;
import com.sunbeam.exception.UserNotFoundException;
import com.sunbeam.model.ApiResponse;
import com.sunbeam.model.User;
import com.sunbeam.repository.UserRepository;
import com.sunbeam.security.SecurityUtils;
import com.sunbeam.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
	@Autowired
	ModelMapper modelMapper;
	
	@Autowired
	SecurityUtils securityUtils;
    private final UserRepository userRepository;

    @Override
    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not logged in"));
    }

    @Override
    public UserResponse getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return mapToUserResponse(user);
    }

//    @Override
//    public List<User> getAllVerifiers() {
//        return userRepository.findByRole(User.Role.VERIFIER);
//    }

    @Override
    public ApiResponse updateUserProfile(@Valid UserUpdateRequest request) {
        User existingUser = getCurrentUser();

        // Manually update all updatable fields to avoid accidental overwrites
        existingUser.setFirstName(request.getFirstName());
        existingUser.setLastName(request.getLastName());
        existingUser.setAddress(request.getAddress());
        existingUser.setDateOfBirth(request.getDateOfBirth());
        existingUser.setGender(request.getGender());
        existingUser.setFatherName(request.getFatherName());
        
        // Update aadhar only if provided and different
        if (request.getAadharNumber() != null && 
            !request.getAadharNumber().equals(existingUser.getAadharNumber())) {
            existingUser.setAadharNumber(request.getAadharNumber());
        }

        userRepository.save(existingUser);

        return new ApiResponse("User Profile Updated Successfully");
    }


    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFirstName() + " " + user.getLastName())
                .email(user.getEmail())
                .role(user.getRole())
//                .address(user.getAddress())
                .build();
    }

    @Override
    public UserResponse getCurrentUserProfile() {
        User currentUser = securityUtils.getCurrentUser();
        
        // Fetch fresh data from DB to avoid stale information
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        return modelMapper.map(user, UserResponse.class);
    }

	

}