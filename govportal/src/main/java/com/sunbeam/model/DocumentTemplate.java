package com.sunbeam.model;



import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class DocumentTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    private DocumentType documentType;
    
    @Lob
    private String htmlContent;
    
    public enum DocumentType {
        INCOME_CERTIFICATE, 
        CASTE_CERTIFICATE
    }
}