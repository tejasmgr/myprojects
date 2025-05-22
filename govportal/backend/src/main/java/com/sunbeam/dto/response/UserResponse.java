package com.sunbeam.dto.response;

import java.time.LocalDate;

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
	private String address;

	private LocalDate dateOfBirth;

	private String gender;

	private String aadharNumber;
}
