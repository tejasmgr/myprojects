package com.sunbeam.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.sunbeam.model.User.Designation;
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
	private String fatherName;
	private boolean enabled;
	private boolean blocked;
	private String address;
	private Designation designation;
	private LocalDate dateOfBirth;
	private String gender;
	private String aadharNumber;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
