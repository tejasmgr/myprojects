package com.sunbeam.repository;

import java.util.List;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


import com.sunbeam.model.User;
import com.sunbeam.model.User.Role;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Page<User> findByRole(Role role, Pageable pageable);
    Optional<User> findById(Long id);
    	
    
//    List<User> findByRole(Role role);
//    List<User> findByRoleAndEnabledTrue(Role role);
    long countByRole(Role citizen);
    long countByBlockedTrue();
  //  User findById(long id);

//    @Query("SELECT u FROM User u WHERE u.role = 'VERIFIER' AND u NOT IN " +
//           "(SELECT a.assignedVerifiers FROM DocumentApplication a WHERE a.status = 'PENDING')")
//    List<User> findAvailableVerifiers();
//	Page<User> findByRole(com.sunbeam.model.User.Role citizen, Pageable pageable);
	long countByRoleAndEnabledTrue(Role verifier);
//	List<User> findByRole(com.sunbeam.model.User.Role verifier);
}

