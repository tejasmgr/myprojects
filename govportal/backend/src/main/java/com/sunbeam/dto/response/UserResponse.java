package com.sunbeam.dto.response;

import com.sunbeam.model.User.Role;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Setter
@Getter
public class UserResponse {
	private Long id;
    private String fullName;
    private String email;
    private Role role;
    private boolean enabled;
    private boolean blocked;
    private String aadharNumber;
}
