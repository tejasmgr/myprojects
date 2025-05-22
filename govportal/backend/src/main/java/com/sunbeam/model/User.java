package com.sunbeam.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class User {
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.CITIZEN;

    @Column(nullable = false)
    private boolean enabled = false;

    @Column(nullable = false)
    private boolean blocked = false;
    
    @Enumerated(EnumType.STRING)
    private Designation designation = null;

    // Citizen-specific fields
    private String address;
    
    private LocalDate dateOfBirth;
    
    private String gender;
    
    private String fatherName;
    
    @Column(unique = true)
    private String aadharNumber;

    // Audit fields
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public enum Role {
        CITIZEN, 
        VERIFIER, 
        ADMIN
    }
    
    public enum Designation {
        JUNIOR_VERIFIER, 
        SENIOR_VERIFIER, 
    }

    // Full name getter
    public String getFullName() {
        return firstName + " " + lastName;
    }
}