package com.sunbeam.model;
 

 import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;
 import lombok.AllArgsConstructor;
 import lombok.Builder;
 import lombok.Data;
 import lombok.NoArgsConstructor;
 

 @Entity
 @Data
 @Builder
 @NoArgsConstructor
 @AllArgsConstructor
 @Table(name = "document_proofs")
 public class DocumentProof {
 
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
 

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "application_id", nullable = false)
  @JsonBackReference
  private DocumentApplication application;
 


  @Column(nullable = false)
  private String fileName;
 

  @Column(nullable = false)
  private String filePath; // Or fileUrl for cloud storage
 

  @Column(nullable = false)
  private String contentType;
 

  //  You might want to add fields like uploadDate, uploader (if different from applicant)
 

 }