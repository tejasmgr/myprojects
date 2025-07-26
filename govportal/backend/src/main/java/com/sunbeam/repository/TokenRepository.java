package com.sunbeam.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sunbeam.model.Token;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long>{
	 Optional<Token> findByToken(String token);
	 void deleteByExpiryDateBefore(LocalDateTime now);
}
