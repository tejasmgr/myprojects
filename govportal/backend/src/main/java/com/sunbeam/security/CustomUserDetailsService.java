package com.sunbeam.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.sunbeam.model.CustomUserDetails;
import com.sunbeam.model.User;
import com.sunbeam.repository.UserRepository;

@Service

public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return new CustomUserDetails(user);
                
        		
        		
//        		.withUsername(user.getEmail())
//                .password(user.getPassword())
//                .roles(user.getRole().name())
//                .accountLocked(user.isBlocked())
//                .disabled(!user.isEnabled())
//                .build();
    }
}
