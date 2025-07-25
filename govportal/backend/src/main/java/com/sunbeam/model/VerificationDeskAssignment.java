//package com.sunbeam.model;
//
//import jakarta.persistence.*;
//import lombok.Data;
//
//@Entity
//@Data
//public class VerificationDeskAssignment {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//    
//    @ManyToOne
//    private DocumentApplication application;
//    
//    @ManyToOne
//    private User verifier;
//    
//    private String deskLevel; // DESK_1, DESK_2
//}