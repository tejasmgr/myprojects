package com.sunbeam.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.sunbeam.model.Role;
import com.sunbeam.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByRole(Role role);
    List<User> findByRoleAndEnabledTrue(Role role);
    long countByRole(com.sunbeam.model.User.Role citizen);
    long countByBlockedTrue();

    @Query("SELECT u FROM User u WHERE u.role = 'VERIFIER' AND u NOT IN " +
           "(SELECT a.assignedVerifiers FROM DocumentApplication a WHERE a.status = 'PENDING')")
    List<User> findAvailableVerifiers();
	Page<User> findByRole(com.sunbeam.model.User.Role citizen, Pageable pageable);
	long countByRoleAndEnabledTrue(com.sunbeam.model.User.Role verifier);
	List<User> findByRole(com.sunbeam.model.User.Role verifier);
}

