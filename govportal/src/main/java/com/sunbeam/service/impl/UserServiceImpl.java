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
    public void updateUserProfile(User updatedUser) {
        User existingUser = getCurrentUser();
        existingUser.setFirstName(updatedUser.getFirstName());
        existingUser.setLastName(updatedUser.getLastName());
        existingUser.setAddress(updatedUser.getAddress());
        userRepository.save(existingUser);
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

	@Override
	public ApiResponse updateUserProfile(@Valid UserUpdateRequest request) {
		User existingUser = getCurrentUser();
		existingUser.setFirstName(request .getFirstName());
        existingUser.setLastName(request.getLastName());
        existingUser.setAddress(request.getAddress());
		return new ApiResponse("User Profile Updated Successfully");
	}

}