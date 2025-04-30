package com.sunbeam.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sunbeam.model.DocumentProof;

@Repository
public interface DocumentProofRepository extends JpaRepository<DocumentProof, Long>{

}
