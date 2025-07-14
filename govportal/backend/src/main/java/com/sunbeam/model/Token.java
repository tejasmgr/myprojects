package com.sunbeam.model;



import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Token {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true)
    private String token;
    
    @Enumerated(EnumType.STRING)
    private TokenType type;
    
    private LocalDateTime expiryDate;
    
    @ManyToOne
    private User user;

    public enum TokenType {
        VERIFICATION, PASSWORD_RESET
    }
}